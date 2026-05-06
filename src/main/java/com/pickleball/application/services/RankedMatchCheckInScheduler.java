package com.pickleball.application.services;

import com.pickleball.domain.entities.*;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankedMatchCheckInScheduler {

    private final BookingRepository bookingRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;

    @Scheduled(fixedDelay = 60_000) // 1 minute
    @Transactional
    public void processNoShows() {
        LocalDateTime timeThreshold = LocalDateTime.now().minusMinutes(10);
        List<Booking> expiredBookings = bookingRepository.findExpiredRankedNoShows(timeThreshold);

        if (expiredBookings.isEmpty()) {
            return;
        }

        for (Booking booking : expiredBookings) {
            try {
                handleBookingNoShows(booking);
            } catch (Exception e) {
                log.error("Failed to process no-shows for bookingId={}: {}", booking.getId(), e.getMessage());
            }
        }
    }

    private void handleBookingNoShows(Booking booking) {
        List<BookingParticipant> noShowPlayers = new ArrayList<>();
        List<BookingParticipant> checkedInPlayers = new ArrayList<>();
        BookingParticipant referee = null;

        for (BookingParticipant participant : booking.getParticipants()) {
            if (participant.getRole() == ParticipantRole.REFEREE) {
                referee = participant;
            } else if (participant.getRole() == ParticipantRole.PLAYER || participant.getRole() == ParticipantRole.HOST) {
                if (participant.getJoinStatus() == JoinStatus.CHECKED_IN) {
                    checkedInPlayers.add(participant);
                } else if (participant.getJoinStatus() == JoinStatus.PAID) {
                    noShowPlayers.add(participant);
                }
            }
        }

             if (noShowPlayers.isEmpty()) {
            return;
        }

        log.info("Found {} no-show players for Ranked Match bookingId={}", noShowPlayers.size(), booking.getId());

        // 1. Process forfeits
        for (BookingParticipant badPlayer : noShowPlayers) {
            badPlayer.markAsForfeited();
            
            // Record Penalty Transaction
            if (badPlayer.getDepositAmount() != null && badPlayer.getDepositAmount().getAmount().compareTo(BigDecimal.ZERO) > 0) {
                Transaction penaltyTx = Transaction.builder()
                        .userId(badPlayer.getUserId())
                        .bookingId(booking.getId())
                        .amount(badPlayer.getDepositAmount().getAmount())
                        .type("PENALTY")
                        .status("SUCCESS")
                        .description("Bùng kèo - Không check-in Ranked Match")
                        .createdAt(LocalDateTime.now())
                        .build();
                transactionRepository.save(penaltyTx);
            }
        }

        // 2. Mark Booking as COMPLETED (time slot used as Free Play) and RankedMatch as CANCELLED
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        RankedMatch rankedMatch = rankedMatchRepository.findByBookingId(booking.getId()).orElse(null);
        if (rankedMatch != null) {
            rankedMatch.setStatus(MatchStatus.CANCELLED);
            rankedMatchRepository.save(rankedMatch);
        }

        // 3. Distribute Payouts (System already collected 100% implicitly via 4 * 25% deposits)
        // Venue gets 100% of venue fee
        processVenuePayout(booking);

        // Referee gets 50% of referee fee
        BigDecimal refereeCompensation = BigDecimal.ZERO;
        if (booking.getRefereeFee() != null && booking.getRefereeFee().getAmount().compareTo(BigDecimal.ZERO) > 0) {
            refereeCompensation = booking.getRefereeFee().getAmount().multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
            if (referee != null) {
                creditWalletAndLog(referee.getUserId(), booking.getId(), refereeCompensation, "PAYOUT", "Phí bồi thường trọng tài (Ranked Cancelled)");
            }
        }

        // 4. Compensation for Checked-in Players
        // Total Cost collected from all players
        BigDecimal totalCollected = booking.getTotalCost() != null ? booking.getTotalCost().getAmount() : BigDecimal.ZERO;
        
        // Calculate what was paid to venue (following SettlementService logic: netAmount = venueFee - 20% platform deduction)
        BigDecimal venueGross = booking.getVenueFee() != null ? booking.getVenueFee().getAmount() : BigDecimal.ZERO;
        BigDecimal venueNetPayout = venueGross.subtract(venueGross.multiply(new BigDecimal("0.20")));

        // Remaining Pool = Total Collected - Venue Payout - Referee Payout
        BigDecimal compensationPool = totalCollected.subtract(venueNetPayout).subtract(refereeCompensation);

        if (compensationPool.compareTo(BigDecimal.ZERO) > 0 && !checkedInPlayers.isEmpty()) {
            BigDecimal amountPerPlayer = compensationPool.divide(new BigDecimal(checkedInPlayers.size()), 2, RoundingMode.FLOOR);
            for (BookingParticipant goodPlayer : checkedInPlayers) {
                creditWalletAndLog(goodPlayer.getUserId(), booking.getId(), amountPerPlayer, "REFUND", "Đền bù huỷ Rank do có người bùng kèo");
            }
        }

        log.info("Processed No-Show for bookingId={}. Venue paid, Referee compensated {}, Checked-in players compensated", booking.getId(), refereeCompensation);
    }

    private void processVenuePayout(Booking booking) {
        if (booking.getVenueFee() == null || booking.getVenueFee().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Court court = courtRepository.findById(booking.getCourtId())
                .orElseThrow(() -> new IllegalArgumentException("Court not found"));
        
        Venue venue = venueRepository.findById(court.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        Long ownerId = venue.getOwnerId();
        BigDecimal grossAmount = booking.getVenueFee().getAmount();
        BigDecimal platformFee = grossAmount.multiply(new BigDecimal("0.20"));
        BigDecimal netAmount = grossAmount.subtract(platformFee);

        creditWalletAndLog(ownerId, booking.getId(), netAmount, "PAYOUT", "Thanh toán sân " + venue.getName() + " (Đã huỷ Rank, chuyển Free Play)");
    }

    private void creditWalletAndLog(Long userId, Long bookingId, BigDecimal amount, String type, String description) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .userId(userId)
                            .balance(BigDecimal.ZERO)
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return walletRepository.save(newWallet);
                });

        wallet.credit(amount);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .bookingId(bookingId)
                .amount(amount)
                .type(type)
                .status("SUCCESS")
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }
}
