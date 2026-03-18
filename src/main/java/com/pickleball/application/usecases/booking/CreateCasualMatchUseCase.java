package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.Court;
import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtPricingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.PlayerRepository;
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
 * Use Case: Create Casual Match (WORKFLOW II.2)
 *
 * Flow:
 * 1. Host selects "Create Match" with Ranked = OFF
 * 2. Host deposits 25% of court fee
 * 3. System finds 3 matching players (Elo +/-200)
 * 4. Each player deposits 25% -> when 4 confirmed -> CONFIRMED
 * 5. Match happens (no referee, no Elo changes)
 */
public class CreateCasualMatchUseCase {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final CourtPricingRepository courtPricingRepository;
    private final PlayerRepository playerRepository;
    private final VenueRepository venueRepository;
    private final PriceCalculationService priceCalculationService;
    private final PaymentService paymentService;
    private final MatchmakingService matchmakingService;

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.20");
    private static final BigDecimal DEPOSIT_PERCENTAGE = new BigDecimal("0.25");
    private static final int MAX_CANDIDATES = 20;

    public CreateCasualMatchUseCase(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            CourtPricingRepository courtPricingRepository,
            PlayerRepository playerRepository,
            VenueRepository venueRepository,
            PriceCalculationService priceCalculationService,
            PaymentService paymentService,
            MatchmakingService matchmakingService) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.courtPricingRepository = courtPricingRepository;
        this.playerRepository = playerRepository;
        this.venueRepository = venueRepository;
        this.priceCalculationService = priceCalculationService;
        this.paymentService = paymentService;
        this.matchmakingService = matchmakingService;
    }

    public CasualMatchResult execute(Long courtId, LocalDateTime startTime, LocalDateTime endTime, Long hostUserId,
            String notes) {
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

        // 4. Calculate venue fee
        Money venueFee = calculateVenueFee(courtId, startTime, endTime);

        // 5. Calculate deposit per player (25%)
        Money depositPerPlayer = calculateDeposit(venueFee);

        // 6. Create booking CASUAL with PENDING status
        Booking booking = Booking.builder()
                .courtId(courtId)
                .startTime(startTime)
                .endTime(endTime)
                .bookingType(BookingType.CASUAL)
                .status(BookingStatus.PENDING)
                .notes(notes)
                .createdAt(LocalDateTime.now())
                .createdByPlayerId(hostUserId)
                .build();

        booking.calculateCosts(venueFee, null, PLATFORM_FEE_PERCENTAGE);
        booking = bookingRepository.save(booking);

        // 7. Add host as participant with 25% deposit
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

        // 8. Process host deposit payment (25%)
        String orderId = "CASUAL_DEPOSIT_" + booking.getId() + "_HOST";
        String description = "Casual match deposit - Court #" + courtId + " - " + startTime.toLocalDate();
        PaymentResult paymentResult = paymentService.createPayment(orderId, depositPerPlayer, description, hostUserId);

        if (paymentResult.success() && "SUCCESS".equals(paymentResult.status())) {
            host.setJoinStatus(JoinStatus.PAID);
        } else if (!paymentResult.success()) {
            booking.cancel();
            booking = bookingRepository.save(booking);
            return new CasualMatchResult(booking, paymentResult, Collections.emptyList(), depositPerPlayer);
        }

        booking = bookingRepository.save(booking);

        // 9. Find matching candidates via MatchmakingService
        List<Player> candidates = findCandidates(hostPlayer, booking.getId());

        return new CasualMatchResult(booking, paymentResult, candidates, depositPerPlayer);
    }

    public List<Player> findCandidates(Player hostPlayer, Long bookingId) {
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
                recentOpponents, hostElo, venueLat, venueLng, null, MAX_CANDIDATES);
    }

    private Money calculateVenueFee(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
        List<CourtPricing> pricings = courtPricingRepository.findByCourtId(courtId);
        if (pricings.isEmpty()) {
            return new Money(new BigDecimal("200000"), "VND");
        }
        long hours = Duration.between(startTime, endTime).toHours();
        if (hours <= 0)
            hours = 1;
        Money pricePerHour = priceCalculationService.calculateSlotPrice(
                pricings, startTime.toLocalTime(), startTime.getDayOfWeek());
        BigDecimal totalAmount = pricePerHour.getAmount().multiply(BigDecimal.valueOf(hours));
        return new Money(totalAmount, pricePerHour.getCurrency());
    }

    private Money calculateDeposit(Money venueFee) {
        BigDecimal depositAmount = venueFee.getAmount()
                .multiply(DEPOSIT_PERCENTAGE)
                .setScale(2, RoundingMode.HALF_UP);
        return new Money(depositAmount, venueFee.getCurrency());
    }

    public record CasualMatchResult(
            Booking booking,
            PaymentResult paymentResult,
            List<Player> candidates,
            Money depositPerPlayer) {
    }
}
