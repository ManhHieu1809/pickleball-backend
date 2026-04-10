package com.pickleball.application.usecases.booking;

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
import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.services.PaymentService.PaymentResult;
import com.pickleball.domain.services.PriceCalculationService;
import com.pickleball.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Use Case: Create Ranked Match (WORKFLOW §II.3)
 *
 * Ranked = Casual + 3 differences:
 * 1. Requires 1 eligible Referee
 * 2. Referee fee included in total cost
 * 3. Elo changes after result is confirmed (handled by UpdateRatingUseCase)
 *
 * Flow:
 * 1. Host selects "Create Match" with Ranked = ON
 * 2. Calculate total cost = Venue Fee + Referee Fee + Platform Fee
 * 3. Host deposits 25% of total cost
 * 4. System finds 3 matching players (reuse MatchmakingService)
 * 5. System finds eligible referees
 * 6. Creates RankedMatch entity linked to Booking
 * 7. Players + Referee join & deposit → auto-confirm when all paid
 */
public class CreateRankedMatchUseCase {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final CourtPricingRepository courtPricingRepository;
    private final PlayerRepository playerRepository;
    private final VenueRepository venueRepository;
    private final RefereeRepository refereeRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final PriceCalculationService priceCalculationService;
    private final PaymentService paymentService;
    private final MatchmakingService matchmakingService;

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.20");
    private static final BigDecimal DEPOSIT_PERCENTAGE = new BigDecimal("0.25");
    private static final BigDecimal REFEREE_FEE_FLAT = new BigDecimal("100000"); // 100,000 VND
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
            PaymentService paymentService,
            MatchmakingService matchmakingService) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.courtPricingRepository = courtPricingRepository;
        this.playerRepository = playerRepository;
        this.venueRepository = venueRepository;
        this.refereeRepository = refereeRepository;
        this.rankedMatchRepository = rankedMatchRepository;
        this.priceCalculationService = priceCalculationService;
        this.paymentService = paymentService;
        this.matchmakingService = matchmakingService;
    }

    public RankedMatchResult execute(Long courtId, LocalDateTime startTime, LocalDateTime endTime,
                                      Long hostUserId, String notes) {
        // 1. Validate court
        courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Court not found"));

        // 2. Validate host is a player
        Player hostPlayer = playerRepository.findByUserId(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("Player profile not found"));

        // 3. Check time slot conflicts
        List<Booking> conflicts = bookingRepository.findConflictingBookings(courtId, startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Time slot is already booked");
        }

        // 4. Check eligible referees exist before creating match
        List<Referee> eligibleReferees = refereeRepository.findEligibleReferees();
        if (eligibleReferees.isEmpty()) {
            throw new IllegalArgumentException(
                    "No eligible referees available. Ranked match requires at least one referee.");
        }

        // 5. Calculate costs: Venue Fee + Referee Fee + Platform Fee
        Money venueFee = calculateVenueFee(courtId, startTime, endTime);
        Money refereeFee = new Money(REFEREE_FEE_FLAT, "VND");

        // 6. Create booking RANKED with PENDING status
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

        // calculateCosts: totalCost = venueFee + refereeFee + platformFee
        booking.calculateCosts(venueFee, refereeFee, PLATFORM_FEE_PERCENTAGE);
        booking = bookingRepository.save(booking);

        // 7. Calculate deposit per player (25% of totalCost)
        Money depositPerPlayer = calculateDeposit(booking.getTotalCost());

        // 8. Add host as participant with deposit
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

        // 9. Process host deposit payment (25% of totalCost)
        String orderId = "RANKED_DEPOSIT_" + booking.getId() + "_HOST";
        String description = "Ranked match deposit - Court #" + courtId + " - " + startTime.toLocalDate();
        PaymentResult paymentResult = paymentService.createPayment(orderId, depositPerPlayer, description, hostUserId);

        if (paymentResult.success() && "SUCCESS".equals(paymentResult.status())) {
            host.setJoinStatus(JoinStatus.PAID);
        } else if (!paymentResult.success()) {
            booking.cancel();
            booking = bookingRepository.save(booking);
            return new RankedMatchResult(booking, null, paymentResult,
                    Collections.emptyList(), Collections.emptyList(), depositPerPlayer);
        }

        booking = bookingRepository.save(booking);

        // 10. Create RankedMatch entity (referee not yet assigned)
        RankedMatch rankedMatch = RankedMatch.builder()
                .bookingId(booking.getId())
                .status(MatchStatus.PENDING)
                .build();
        rankedMatch = rankedMatchRepository.save(rankedMatch);

        // 11. Find player candidates (reuse from Casual)
        List<Player> playerCandidates = findPlayerCandidates(hostPlayer, booking.getId());

        // 12. Find eligible referee candidates
        List<Referee> refereeCandidates = findRefereeCandidates(eligibleReferees, hostUserId);

        return new RankedMatchResult(booking, rankedMatch, paymentResult,
                playerCandidates, refereeCandidates, depositPerPlayer);
    }

    /**
     * Find player candidates using the same MatchmakingService as Casual.
     * Filters: Elo ±200, GPS ≤15km, anti-repetition.
     */
    public List<Player> findPlayerCandidates(Player hostPlayer, Long bookingId) {
        int hostElo = hostPlayer.getCurrentElo();
        int[] eloRange = matchmakingService.calculateEloRange(hostElo);

        List<Player> playersInRange = playerRepository.findByEloRange(eloRange[0], eloRange[1]);
        List<Long> existingParticipants = bookingRepository.findParticipantUserIdsByBookingId(bookingId);

        // Anti-repetition: get recent opponents
        List<Long> recentOpponents = bookingRepository.findRecentOpponentUserIds(
                hostPlayer.getUserId(), matchmakingService.getAntiRepetitionLastN());

        // GPS filter: get venue location from booking's court
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

    /**
     * Find eligible referees for the ranked match.
     * Eligible = testPassed=true, isActive=true, trustScore >= 30
     */
    public List<Referee> findRefereeCandidates(List<Referee> eligibleReferees, Long hostUserId) {
        return eligibleReferees.stream()
                // Exclude host (a player cannot referee their own match)
                .filter(r -> !r.getUserId().equals(hostUserId))
                // Already verified eligible via findEligibleReferees(), but double-check
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

    /**
     * Result record containing all data from ranked match creation.
     */
    public record RankedMatchResult(
            Booking booking,
            RankedMatch rankedMatch,
            PaymentResult paymentResult,
            List<Player> playerCandidates,
            List<Referee> refereeCandidates,
            Money depositPerPlayer) {
    }
}
