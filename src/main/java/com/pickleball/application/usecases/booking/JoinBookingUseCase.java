package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.domain.services.MatchmakingService;
import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.services.PaymentService.PaymentResult;
import com.pickleball.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class JoinBookingUseCase {
    private final BookingRepository bookingRepository;
    private final PlayerRepository playerRepository;
    private final PaymentService paymentService;
    private final MatchmakingService matchmakingService;

    private static final BigDecimal DEPOSIT_PERCENTAGE = new BigDecimal("0.25");

    public JoinBookingUseCase(BookingRepository bookingRepository,
                              PlayerRepository playerRepository,
                              PaymentService paymentService,
                              MatchmakingService matchmakingService) {
        this.bookingRepository = bookingRepository;
        this.playerRepository = playerRepository;
        this.paymentService = paymentService;
        this.matchmakingService = matchmakingService;
    }

    public JoinResult execute(Long bookingId, Long userId) {
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

        // Calculate deposit amount based on booking type
        Money depositAmount;
        if (booking.getBookingType() == BookingType.CASUAL) {
            // Casual: deposit = 25% venue fee
            depositAmount = calculateCasualDeposit(booking);

            // Elo compatibility check for casual matches
            validateEloCompatibility(booking, userId);
        } else {
            // Default for other types
            depositAmount = new Money(new BigDecimal("50000"), "VND");
        }

        // Process deposit payment
        String orderId = "DEPOSIT_" + bookingId + "_USER_" + userId;
        String description = "Match deposit - Booking #" + bookingId;
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
                .role(ParticipantRole.PLAYER)
                .joinStatus(joinStatus)
                .depositAmount(depositAmount)
                .refundAmount(new Money(BigDecimal.ZERO, "VND"))
                .isMatchHost(false)
                .build();

        booking.addParticipant(participant);

        // Auto-confirm when 4 players have PAID (for CASUAL matches)
        if (booking.getBookingType() == BookingType.CASUAL) {
            long paidCount = booking.getParticipants().stream()
                    .filter(p -> p.getJoinStatus() == JoinStatus.PAID)
                    .count();

            if (matchmakingService.isMatchFull((int) paidCount)) {
                booking.confirm(); // Status -> CONFIRMED
            }
        }

        Booking saved = bookingRepository.save(booking);
        return new JoinResult(saved, paymentResult);
    }

    private Money calculateCasualDeposit(Booking booking) {
        if (booking.getVenueFee() == null) {
            return new Money(new BigDecimal("50000"), "VND");
        }
        BigDecimal deposit = booking.getVenueFee().getAmount()
                .multiply(DEPOSIT_PERCENTAGE)
                .setScale(2, RoundingMode.HALF_UP);
        return new Money(deposit, booking.getVenueFee().getCurrency());
    }

    private void validateEloCompatibility(Booking booking, Long userId) {
        // Get host's player profile
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