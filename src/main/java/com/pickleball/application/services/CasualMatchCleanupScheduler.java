package com.pickleball.application.services;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.valueobjects.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled service to cancel expired PENDING casual matches
 * and refund deposits to participants who already paid.
 *
 * Runs every 5 minutes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CasualMatchCleanupScheduler {

    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 300_000) // 5 minutes
    @Transactional
    public void cancelExpiredCasualMatches() {
        List<Booking> expiredBookings = bookingRepository.findExpiredPendingCasual(LocalDateTime.now());

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("Found {} expired PENDING casual matches to cancel", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                cancelAndRefund(booking);
            } catch (Exception e) {
                log.error("Failed to cancel expired casual match bookingId={}: {}", booking.getId(), e.getMessage());
            }
        }
    }

    private void cancelAndRefund(Booking booking) {
        // Refund deposits to all PAID participants
        for (BookingParticipant participant : booking.getParticipants()) {
            if (participant.getJoinStatus() == JoinStatus.PAID && participant.getDepositAmount() != null) {
                Money depositAmount = participant.getDepositAmount();
                if (depositAmount.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    String transactionId = "CASUAL_DEPOSIT_" + booking.getId() + "_USER_" + participant.getUserId();
                    paymentService.refund(transactionId, depositAmount,
                            "Casual match expired - not enough players joined");
                    log.info("Refunded {} to userId={} for expired bookingId={}",
                            depositAmount.getAmount(), participant.getUserId(), booking.getId());
                }
            }
        }

        // Cancel the booking
        booking.cancel();
        bookingRepository.save(booking);
        log.info("Cancelled expired casual match bookingId={}", booking.getId());
    }
}
