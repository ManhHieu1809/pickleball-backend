package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.entities.Referee;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import com.pickleball.domain.repositories.RefereeRepository;
import com.pickleball.domain.services.MatchmakingService;
import com.pickleball.domain.services.TeamBalancingService;
import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.services.PaymentService.PaymentResult;
import com.pickleball.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Use Case: Join a booking (Casual or Ranked).
 *
 * For CASUAL:
 * - Deposit = 25% venue fee
 * - Auto-confirm when 4 players PAID
 *
 * For RANKED (extended logic):
 * - Players deposit = 25% totalCost (venue + referee + platform fee)
 * - Referee can join with role REFEREE
 * - Auto-confirm when 4 players + 1 referee ALL PAID
 * - Referee is assigned to RankedMatch entity when joining
 */
public class JoinBookingUseCase {
    private final BookingRepository bookingRepository;
    private final PlayerRepository playerRepository;
    private final RefereeRepository refereeRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final PaymentService paymentService;
    private final MatchmakingService matchmakingService;
    private final TeamBalancingService teamBalancingService;

    private static final BigDecimal DEPOSIT_PERCENTAGE = new BigDecimal("0.25");

    public JoinBookingUseCase(BookingRepository bookingRepository,
                              PlayerRepository playerRepository,
                              RefereeRepository refereeRepository,
                              RankedMatchRepository rankedMatchRepository,
                              PaymentService paymentService,
                              MatchmakingService matchmakingService,
                              TeamBalancingService teamBalancingService) {
        this.bookingRepository = bookingRepository;
        this.playerRepository = playerRepository;
        this.refereeRepository = refereeRepository;
        this.rankedMatchRepository = rankedMatchRepository;
        this.paymentService = paymentService;
        this.matchmakingService = matchmakingService;
        this.teamBalancingService = teamBalancingService;
    }

    /**
     * Join as a PLAYER (for both Casual and Ranked matches).
     */
    public JoinResult execute(Long bookingId, Long userId) {
        return execute(bookingId, userId, false);
    }

    /**
     * Join a booking as either a Player or Referee.
     *
     * @param bookingId  The booking to join
     * @param userId     The user joining
     * @param asReferee  If true, join as REFEREE (only valid for RANKED bookings)
     */
    public JoinResult execute(Long bookingId, Long userId, boolean asReferee) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Validate booking status
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Booking is not available to join");
        }

        // Check if user already joined
        boolean alreadyJoined = booking.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));
        if (alreadyJoined) {
            throw new IllegalArgumentException("User already joined this booking");
        }

        // Determine role and validate
        ParticipantRole role;
        if (asReferee) {
            role = ParticipantRole.REFEREE;
            validateRefereeJoin(booking, userId);
        } else {
            role = ParticipantRole.PLAYER;
            validatePlayerJoin(booking, userId);
        }

        // Calculate deposit amount based on booking type
        Money depositAmount = calculateDeposit(booking, asReferee);

        // Process deposit payment
        String roleLabel = asReferee ? "REFEREE" : "PLAYER";
        String orderId = "DEPOSIT_" + bookingId + "_" + roleLabel + "_" + userId;
        String description = (asReferee ? "Referee" : "Match") + " deposit - Booking #" + bookingId;
        PaymentResult paymentResult = paymentService.createPayment(orderId, depositAmount, description, userId);

        JoinStatus joinStatus;
        if (paymentResult.success() && "SUCCESS".equals(paymentResult.status())) {
            joinStatus = JoinStatus.PAID;
        } else if (!paymentResult.success()) {
            throw new IllegalArgumentException("Payment failed: " + paymentResult.message());
        } else {
            joinStatus = JoinStatus.PENDING;
        }

        // Add participant
        BookingParticipant participant = BookingParticipant.builder()
                .bookingId(booking.getId())
                .userId(userId)
                .role(role)
                .joinStatus(joinStatus)
                .depositAmount(depositAmount)
                .refundAmount(new Money(BigDecimal.ZERO, "VND"))
                .isMatchHost(false)
                .build();

        booking.addParticipant(participant);

        // For RANKED: assign referee to RankedMatch entity
        if (asReferee && joinStatus == JoinStatus.PAID) {
            assignRefereeToRankedMatch(booking.getId(), userId);
        }

        // Auto-confirm logic based on booking type
        autoConfirmIfReady(booking);

        Booking saved = bookingRepository.save(booking);
        return new JoinResult(saved, paymentResult);
    }

    /**
     * Validate that a player can join this booking.
     */
    private void validatePlayerJoin(Booking booking, Long userId) {
        if (booking.getBookingType() == BookingType.CASUAL
                || booking.getBookingType() == BookingType.RANKED) {
            // Elo compatibility check
            validateEloCompatibility(booking, userId);

            // Check if player slots are full (max 4 players including host)
            long playerCount = booking.getParticipants().stream()
                    .filter(p -> p.getRole() != ParticipantRole.REFEREE)
                    .count();
            if (playerCount >= matchmakingService.getMaxPlayersPerMatch()) {
                throw new IllegalArgumentException("Match is already full (4 players max)");
            }
        }
    }

    /**
     * Validate that a referee can join this booking.
     * Only valid for RANKED bookings.
     */
    private void validateRefereeJoin(Booking booking, Long userId) {
        // Only RANKED bookings accept referees
        if (booking.getBookingType() != BookingType.RANKED) {
            throw new IllegalArgumentException("Only ranked matches require a referee");
        }

        // Check if referee already assigned
        boolean hasReferee = booking.getParticipants().stream()
                .anyMatch(p -> p.getRole() == ParticipantRole.REFEREE);
        if (hasReferee) {
            throw new IllegalArgumentException("A referee is already assigned to this match");
        }

        // Validate referee eligibility
        Referee referee = refereeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a registered referee"));

        if (!referee.isEligibleForMatch()) {
            throw new IllegalArgumentException(
                    "Referee is not eligible for matches (test not passed, inactive, or trust score too low)");
        }
    }

    /**
     * Calculate deposit based on booking type and role.
     */
    private Money calculateDeposit(Booking booking, boolean asReferee) {
        if (booking.getBookingType() == BookingType.CASUAL) {
            return calculateCasualDeposit(booking);
        } else if (booking.getBookingType() == BookingType.RANKED) {
            return calculateRankedDeposit(booking);
        }
        // Default for other types
        return new Money(new BigDecimal("50000"), "VND");
    }

    /**
     * Casual deposit = 25% of venue fee.
     */
    private Money calculateCasualDeposit(Booking booking) {
        if (booking.getVenueFee() == null) {
            return new Money(new BigDecimal("50000"), "VND");
        }
        BigDecimal deposit = booking.getVenueFee().getAmount()
                .multiply(DEPOSIT_PERCENTAGE)
                .setScale(0, RoundingMode.HALF_UP);
        return new Money(deposit, booking.getVenueFee().getCurrency());
    }

    /**
     * Ranked deposit = 25% of totalCost (venue + referee + platform fee).
     * All participants (players + referee) pay the same deposit.
     */
    private Money calculateRankedDeposit(Booking booking) {
        if (booking.getTotalCost() == null) {
            return new Money(new BigDecimal("50000"), "VND");
        }
        BigDecimal deposit = booking.getTotalCost().getAmount()
                .multiply(DEPOSIT_PERCENTAGE)
                .setScale(0, RoundingMode.HALF_UP);
        return new Money(deposit, booking.getTotalCost().getCurrency());
    }

    /**
     * Assign referee to the RankedMatch entity when they join.
     */
    private void assignRefereeToRankedMatch(Long bookingId, Long refereeUserId) {
        RankedMatch rankedMatch = rankedMatchRepository.findByBookingId(bookingId)
                .orElse(null);
        if (rankedMatch != null && !rankedMatch.hasReferee()) {
            rankedMatch.assignReferee(refereeUserId);
            rankedMatchRepository.save(rankedMatch);
        }
    }

    /**
     * Auto-confirm the booking when enough participants have paid.
     *
     * CASUAL: 4 players PAID → CONFIRMED
     * RANKED: 4 players PAID + 1 referee PAID → CONFIRMED
     */
    private void autoConfirmIfReady(Booking booking) {
        long paidPlayerCount = booking.getParticipants().stream()
                .filter(p -> p.getRole() != ParticipantRole.REFEREE)
                .filter(p -> p.getJoinStatus() == JoinStatus.PAID)
                .count();

        if (booking.getBookingType() == BookingType.CASUAL) {
            if (matchmakingService.isMatchFull((int) paidPlayerCount)) {
                booking.confirm(); // Status -> CONFIRMED
            }
        } else if (booking.getBookingType() == BookingType.RANKED) {
            boolean refereePaid = booking.getParticipants().stream()
                    .filter(p -> p.getRole() == ParticipantRole.REFEREE)
                    .anyMatch(p -> p.getJoinStatus() == JoinStatus.PAID);

            // Ranked requires 4 players + 1 referee all PAID
            if (matchmakingService.isMatchFull((int) paidPlayerCount) && refereePaid) {
                booking.confirm(); // Status -> CONFIRMED
                teamBalancingService.balanceTeams(booking); // Balance players into Team A and B
            }
        }
    }

    private void validateEloCompatibility(Booking booking, Long userId) {
        Long hostUserId = booking.getCreatedByPlayerId();
        if (hostUserId == null) return;

        Player hostPlayer = playerRepository.findByUserId(hostUserId).orElse(null);
        Player joiningPlayer = playerRepository.findByUserId(userId).orElse(null);

        if (hostPlayer != null && joiningPlayer != null) {
            if (!matchmakingService.isEloCompatible(joiningPlayer.getCurrentElo(), hostPlayer.getCurrentElo())) {
                throw new IllegalArgumentException(
                        "Elo difference too large. Your Elo: " + joiningPlayer.getCurrentElo()
                                + ", Host Elo: " + hostPlayer.getCurrentElo()
                                + ". Max difference: 200");
            }
        }
    }

    public record JoinResult(
            Booking booking,
            PaymentResult paymentResult
    ) {}
}