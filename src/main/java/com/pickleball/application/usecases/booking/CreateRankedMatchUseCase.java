package com.pickleball.application.usecases.booking;

import com.pickleball.application.usecases.wallet.PayWithWalletUseCase;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.Court;
import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.entities.Referee;
import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtPricingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import com.pickleball.domain.repositories.RefereeRepository;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.services.MatchmakingService;
import com.pickleball.domain.services.PaymentService.PaymentResult;
import com.pickleball.domain.services.PriceCalculationService;
import com.pickleball.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class CreateRankedMatchUseCase {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final CourtPricingRepository courtPricingRepository;
    private final PlayerRepository playerRepository;
    private final VenueRepository venueRepository;
    private final RefereeRepository refereeRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final PriceCalculationService priceCalculationService;
    private final PayWithWalletUseCase payWithWalletUseCase;
    private final MatchmakingService matchmakingService;

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.20");
    private static final BigDecimal DEPOSIT_PERCENTAGE = new BigDecimal("0.25");
    private static final BigDecimal REFEREE_FEE_FLAT = new BigDecimal("100000");
    private static final int MAX_PLAYER_CANDIDATES = 20;
    private static final int MAX_REFEREE_CANDIDATES = 10;

    public CreateRankedMatchUseCase(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            CourtPricingRepository courtPricingRepository,
            PlayerRepository playerRepository,
            VenueRepository venueRepository,
            RefereeRepository refereeRepository,
            RankedMatchRepository rankedMatchRepository,
            PriceCalculationService priceCalculationService,
            PayWithWalletUseCase payWithWalletUseCase,
            MatchmakingService matchmakingService) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.courtPricingRepository = courtPricingRepository;
        this.playerRepository = playerRepository;
        this.venueRepository = venueRepository;
        this.refereeRepository = refereeRepository;
        this.rankedMatchRepository = rankedMatchRepository;
        this.priceCalculationService = priceCalculationService;
        this.payWithWalletUseCase = payWithWalletUseCase;
        this.matchmakingService = matchmakingService;
    }

    public RankedMatchResult execute(Long courtId, LocalDateTime startTime, LocalDateTime endTime,
                                      Long hostUserId, String notes) {
        courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Court not found"));

        Player hostPlayer = playerRepository.findByUserId(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("Player profile not found"));

        List<Booking> conflicts = bookingRepository.findConflictingBookings(courtId, startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Time slot is already booked");
        }

        List<Referee> eligibleReferees = refereeRepository.findEligibleReferees();
        if (eligibleReferees.isEmpty()) {
            throw new IllegalArgumentException(
                    "No eligible referees available. Ranked match requires at least one referee.");
        }

        Money venueFee = calculateVenueFee(courtId, startTime, endTime);
        Money refereeFee = new Money(REFEREE_FEE_FLAT, "VND");

        Booking booking = Booking.builder()
                .courtId(courtId)
                .startTime(startTime)
                .endTime(endTime)
                .bookingType(BookingType.RANKED)
                .status(BookingStatus.PENDING)
                .notes(notes)
                .createdAt(LocalDateTime.now())
                .createdByPlayerId(hostUserId)
                .build();

        booking.calculateCosts(venueFee, refereeFee, PLATFORM_FEE_PERCENTAGE);
        booking = bookingRepository.save(booking);

        Money depositPerPlayer = calculateDeposit(booking.getTotalCost());

        BookingParticipant host = BookingParticipant.builder()
                .bookingId(booking.getId())
                .userId(hostUserId)
                .role(ParticipantRole.HOST)
                .joinStatus(JoinStatus.PENDING)
                .isMatchHost(true)
                .depositAmount(depositPerPlayer)
                .refundAmount(new Money(BigDecimal.ZERO, "VND"))
                .build();
        booking.addParticipant(host);

        String description = "Ranked match deposit - Court #" + courtId + " - " + startTime.toLocalDate();
        PaymentResult paymentResult;
        try {
            String transactionId = payWithWalletUseCase.execute(
                    hostUserId,
                    depositPerPlayer.getAmount(),
                    booking.getId(),
                    description
            );
            paymentResult = PaymentResult.success(transactionId, depositPerPlayer);
            host.setJoinStatus(JoinStatus.PAID);
        } catch (Exception e) {
            paymentResult = PaymentResult.failed("Thanh toán thất bại: " + e.getMessage());
            booking.cancel();
            booking = bookingRepository.save(booking);
            return new RankedMatchResult(booking, null, paymentResult,
                    Collections.emptyList(), Collections.emptyList(), depositPerPlayer);
        }

        booking = bookingRepository.save(booking);

        RankedMatch rankedMatch = RankedMatch.builder()
                .bookingId(booking.getId())
                .status(MatchStatus.PENDING)
                .build();
        rankedMatch = rankedMatchRepository.save(rankedMatch);

        List<Player> playerCandidates = findPlayerCandidates(hostPlayer, booking.getId());

        List<Referee> refereeCandidates = findRefereeCandidates(eligibleReferees, hostUserId);

        return new RankedMatchResult(booking, rankedMatch, paymentResult,
                playerCandidates, refereeCandidates, depositPerPlayer);
    }

    public List<Player> findPlayerCandidates(Player hostPlayer, Long bookingId) {
        int hostElo = hostPlayer.getCurrentElo();
        int[] eloRange = matchmakingService.calculateEloRange(hostElo);

        List<Player> playersInRange = playerRepository.findByEloRange(eloRange[0], eloRange[1]);
        List<Long> existingParticipants = bookingRepository.findParticipantUserIdsByBookingId(bookingId);

        List<Long> recentOpponents = bookingRepository.findRecentOpponentUserIds(
                hostPlayer.getUserId(), matchmakingService.getAntiRepetitionLastN());

        Double venueLat = null;
        Double venueLng = null;
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking != null) {
            Court court = courtRepository.findById(booking.getCourtId()).orElse(null);
            if (court != null) {
                Venue venue = venueRepository.findById(court.getVenueId()).orElse(null);
                if (venue != null && venue.getLocation() != null) {
                    venueLat = venue.getLocation().getLatitude().doubleValue();
                    venueLng = venue.getLocation().getLongitude().doubleValue();
                }
            }
        }

        return matchmakingService.findMatchingPlayers(
                playersInRange, hostPlayer.getUserId(), existingParticipants,
                recentOpponents, hostElo, venueLat, venueLng, null, MAX_PLAYER_CANDIDATES);
    }

    public List<Referee> findRefereeCandidates(List<Referee> eligibleReferees, Long hostUserId) {
        return eligibleReferees.stream()
                .filter(r -> !r.getUserId().equals(hostUserId))
                .filter(Referee::isEligibleForMatch)
                .limit(MAX_REFEREE_CANDIDATES)
                .toList();
    }

    private Money calculateVenueFee(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
        List<CourtPricing> pricings = courtPricingRepository.findByCourtId(courtId);
        if (pricings.isEmpty()) {
            return new Money(new BigDecimal("200000"), "VND");
        }
        long hours = Duration.between(startTime, endTime).toHours();
        if (hours <= 0) hours = 1;
        Money pricePerHour = priceCalculationService.calculateSlotPrice(
                pricings, startTime.toLocalTime(), startTime.getDayOfWeek());
        BigDecimal totalAmount = pricePerHour.getAmount().multiply(BigDecimal.valueOf(hours));
        return new Money(totalAmount, pricePerHour.getCurrency());
    }

    private Money calculateDeposit(Money totalCost) {
        BigDecimal depositAmount = totalCost.getAmount()
                .multiply(DEPOSIT_PERCENTAGE)
                .setScale(0, RoundingMode.HALF_UP);
        return new Money(depositAmount, totalCost.getCurrency());
    }

    public record RankedMatchResult(
            Booking booking,
            RankedMatch rankedMatch,
            PaymentResult paymentResult,
            List<Player> playerCandidates,
            List<Referee> refereeCandidates,
            Money depositPerPlayer) {
    }
}
