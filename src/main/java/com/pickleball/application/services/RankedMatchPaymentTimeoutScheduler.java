package com.pickleball.application.services;

import com.pickleball.application.usecases.matchmaking.JoinMatchmakingQueueUseCase;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class RankedMatchPaymentTimeoutScheduler {

    private final BookingRepository bookingRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final PaymentService paymentService;
    private final JoinMatchmakingQueueUseCase joinMatchmakingQueueUseCase;
    private final PlayerRepository playerRepository;

    @Scheduled(fixedDelay = 60_000) // 1 minute
    @Transactional
    public void cancelUnpaidRankedMatches() {
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<Booking> expiredBookings = bookingRepository.findExpiredPendingRanked(twoMinutesAgo);

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("Found {} expired PENDING ranked matches (unpaid) to cancel", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                cancelAndRefundRequeue(booking);
            } catch (Exception e) {
                log.error("Failed to cancel expired ranked match bookingId={}: {}", booking.getId(), e.getMessage());
            }
        }
    }

    private void cancelAndRefundRequeue(Booking booking) {
        // Find paid members to refund and requeue
        for (BookingParticipant participant : booking.getParticipants()) {
            if (participant.getJoinStatus() == JoinStatus.PAID && participant.getDepositAmount() != null) {
                Money depositAmount = participant.getDepositAmount();
                if (depositAmount.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    String transactionId = "RANKED_DEPOSIT_" + booking.getId() + "_USER_" + participant.getUserId();
                    paymentService.refund(transactionId, depositAmount, "Ranked match expired - players failed to pay in time");
                    log.info("Refunded {} to userId={} for expired ranked bookingId={}",
                            depositAmount.getAmount(), participant.getUserId(), booking.getId());
                }

                // Re-queue Paid players as per requirements
                if (participant.getRole() == ParticipantRole.PLAYER || participant.getRole() == ParticipantRole.HOST) {
                    Player p = playerRepository.findByUserId(participant.getUserId()).orElse(null);
                    if (p != null) {
                        try {
                            double lat = p.getLastLatitude() != null ? p.getLastLatitude() : 0.0;
                            double lng = p.getLastLongitude() != null ? p.getLastLongitude() : 0.0;
                            joinMatchmakingQueueUseCase.execute(participant.getUserId(), ParticipantRole.PLAYER, lat, lng);
                            log.info("Re-queued player userId={} due to cancelled match", participant.getUserId());
                        } catch (Exception e) {
                            log.warn("Could not re-queue player userId={}: {}", participant.getUserId(), e.getMessage());
                        }
                    }
                }
            } else if (participant.getRole() == ParticipantRole.REFEREE) {
                // Re-queue the referee
                try {
                    Player p = playerRepository.findByUserId(participant.getUserId()).orElse(null);
                    double lat = (p != null && p.getLastLatitude() != null) ? p.getLastLatitude() : 0.0;
                    double lng = (p != null && p.getLastLongitude() != null) ? p.getLastLongitude() : 0.0;
                    joinMatchmakingQueueUseCase.execute(participant.getUserId(), ParticipantRole.REFEREE, lat, lng);
                    log.info("Re-queued referee userId={} due to cancelled match", participant.getUserId());
                } catch (Exception e) {
                    log.warn("Could not re-queue referee userId={}: {}", participant.getUserId(), e.getMessage());
                }
            }
        }

        // Cancel the booking and mark the ranked match as CANCELLED
        booking.cancel();
        bookingRepository.save(booking);

        RankedMatch rankedMatch = rankedMatchRepository.findByBookingId(booking.getId()).orElse(null);
        if (rankedMatch != null) {
            rankedMatch.setStatus(MatchStatus.CANCELLED);
            rankedMatchRepository.save(rankedMatch);
        }

        log.info("Successfully Cancelled expired pending ranked match bookingId={}", booking.getId());
    }
}

