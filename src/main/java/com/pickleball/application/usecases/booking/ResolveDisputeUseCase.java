package com.pickleball.application.usecases.booking;

import com.pickleball.application.services.SettlementService;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.services.PaymentService.PaymentResult;
import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.entities.Referee;
import com.pickleball.domain.valueobjects.Money;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.DisputeDecision;
import com.pickleball.domain.enums.DisputeStatus;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.MatchDisputeRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import com.pickleball.domain.repositories.RefereeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class ResolveDisputeUseCase {

    private final MatchDisputeRepository matchDisputeRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final BookingRepository bookingRepository;
    private final RefereeRepository refereeRepository;
    private final UpdateEloUseCase updateEloUseCase;
    private final SettlementService settlementService;
    private final PaymentService paymentService;

    @Transactional
    public MatchDispute execute(Long disputeId, Long adminId, DisputeDecision decision, String adminDecisionText) {
        MatchDispute dispute = matchDisputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + disputeId));

        if (dispute.getStatus() == DisputeStatus.RESOLVED) {
            throw new IllegalStateException("Dispute is already resolved");
        }

        RankedMatch match = rankedMatchRepository.findById(dispute.getRankedMatchId())
                .orElseThrow(() -> new IllegalArgumentException("Ranked match not found"));

        Booking booking = bookingRepository.findById(match.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (decision == DisputeDecision.UPHOLD) {
            match.setStatus(MatchStatus.RESOLVED);
            rankedMatchRepository.save(match);
            updateEloUseCase.execute(match.getBookingId());
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
            settlementService.processSettlement(match.getBookingId());

            updateRefereeTrust(match.getRefereeId(), true);

        } else if (decision == DisputeDecision.OVERTURN) {
            match.setStatus(MatchStatus.CANCELLED);
            rankedMatchRepository.save(match);

            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            if (booking.getTotalCost() != null && booking.getTotalCost().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                 String transactionId = "BOOKING_" + booking.getId();
                 Money refundAmount = booking.getTotalCost();
                 try {
                     paymentService.refund(transactionId, refundAmount, "Match Dispute OVERTURNED by Admin");
                 } catch (Exception e) {
                     System.err.println("Failed to process refund: " + e.getMessage());
                 }
            }

            updateRefereeTrust(match.getRefereeId(), false);
        }

        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolvedByAdminId(adminId);
        dispute.setAdminDecision(adminDecisionText);
        dispute.setDecisionType(decision);
        dispute.setResolvedAt(LocalDateTime.now());

        return matchDisputeRepository.save(dispute);
    }

    private void updateRefereeTrust(Long refereeId, boolean positive) {
        if (refereeId == null) return;
        refereeRepository.findByUserId(refereeId).ifPresent(referee -> {
            if (positive) {
                 referee.incrementTrustScore();
            } else {
                 referee.decrementTrustScore(new BigDecimal("5.00"));
            }
            refereeRepository.save(referee);
        });
    }
}
