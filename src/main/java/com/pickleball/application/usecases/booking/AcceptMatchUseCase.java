package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.entities.Referee;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import com.pickleball.domain.repositories.RefereeRepository;
import com.pickleball.domain.services.MatchmakingService;
import com.pickleball.domain.services.PaymentService.PaymentResult;
import com.pickleball.domain.services.TeamBalancingService;
import com.pickleball.application.usecases.wallet.PayWithWalletUseCase;
import com.pickleball.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AcceptMatchUseCase {
    private final BookingRepository bookingRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final PayWithWalletUseCase payWithWalletUseCase;
    private final MatchmakingService matchmakingService;
    private final TeamBalancingService teamBalancingService;
    private final RefereeRepository refereeRepository;

    private static final BigDecimal DEPOSIT_PERCENTAGE = new BigDecimal("0.25");

    public AcceptMatchUseCase(BookingRepository bookingRepository,
                              RankedMatchRepository rankedMatchRepository,
                              PayWithWalletUseCase payWithWalletUseCase,
                              MatchmakingService matchmakingService,
                              TeamBalancingService teamBalancingService,
                              RefereeRepository refereeRepository) {
        this.bookingRepository = bookingRepository;
        this.rankedMatchRepository = rankedMatchRepository;
        this.payWithWalletUseCase = payWithWalletUseCase;
        this.matchmakingService = matchmakingService;
        this.teamBalancingService = teamBalancingService;
        this.refereeRepository = refereeRepository;
    }

    public PaymentResult execute(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Booking is not pending, cannot accept");
        }

        BookingParticipant participant = booking.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not part of this booking"));

        if (participant.getJoinStatus() == JoinStatus.PAID) {
            throw new IllegalArgumentException("User has already accepted and paid for this match");
        }
        if (participant.getJoinStatus() != JoinStatus.PENDING) {
            throw new IllegalArgumentException("User cannot accept match from status: " + participant.getJoinStatus());
        }

        boolean asReferee = participant.getRole() == ParticipantRole.REFEREE;
        Money depositAmount = calculateDeposit(booking, asReferee);

        String description = (asReferee ? "Referee" : "Match") + " deposit - Booking #" + bookingId;
        PaymentResult paymentResult;
        try {
            String transactionId = payWithWalletUseCase.execute(
                    userId,
                    depositAmount.getAmount(),
                    bookingId,
                    description
            );
            paymentResult = PaymentResult.success(transactionId, depositAmount);
        } catch (Exception e) {
            throw new IllegalStateException("Thanh toán thất bại: " + e.getMessage());
        }

        if (paymentResult.success() && "SUCCESS".equals(paymentResult.status())) {
            participant.setJoinStatus(JoinStatus.PAID);
            participant.setDepositAmount(depositAmount);

            if (asReferee) {
                assignRefereeToRankedMatch(booking.getId(), userId);
            }

            autoConfirmIfReady(booking);
            bookingRepository.save(booking);
        } else {
            throw new IllegalArgumentException("Payment failed: " + paymentResult.message());
        }

        return paymentResult;
    }

    private Money calculateDeposit(Booking booking, boolean asReferee) {
        if (booking.getBookingType() == BookingType.CASUAL) {
            if (booking.getVenueFee() == null) return new Money(new BigDecimal("50000"), "VND");
            BigDecimal deposit = booking.getVenueFee().getAmount().multiply(DEPOSIT_PERCENTAGE).setScale(0, RoundingMode.HALF_UP);
            return new Money(deposit, booking.getVenueFee().getCurrency());
        } else if (booking.getBookingType() == BookingType.RANKED) {
            if (booking.getTotalCost() == null) return new Money(new BigDecimal("50000"), "VND");
            BigDecimal deposit = booking.getTotalCost().getAmount().multiply(DEPOSIT_PERCENTAGE).setScale(0, RoundingMode.HALF_UP);
            return new Money(deposit, booking.getTotalCost().getCurrency());
        }
        return new Money(new BigDecimal("50000"), "VND");
    }

    private void assignRefereeToRankedMatch(Long bookingId, Long refereeUserId) {
        RankedMatch rankedMatch = rankedMatchRepository.findByBookingId(bookingId).orElse(null);
        if (rankedMatch != null && !rankedMatch.hasReferee()) {
            rankedMatch.assignReferee(refereeUserId);
            rankedMatchRepository.save(rankedMatch);
        }
    }

    private void autoConfirmIfReady(Booking booking) {
        long paidPlayerCount = booking.getParticipants().stream()
                .filter(p -> p.getRole() != ParticipantRole.REFEREE)
                .filter(p -> p.getJoinStatus() == JoinStatus.PAID)
                .count();

        if (booking.getBookingType() == BookingType.CASUAL) {
            if (matchmakingService.isMatchFull((int) paidPlayerCount)) {
                booking.confirm();
            }
        } else if (booking.getBookingType() == BookingType.RANKED) {
            boolean hasReferee = booking.getParticipants().stream()
                    .anyMatch(p -> p.getRole() == ParticipantRole.REFEREE);

            if (matchmakingService.isMatchFull((int) paidPlayerCount)) {
                if (!hasReferee) {
                    autoAssignReferee(booking);
                }

                boolean refereePaid = booking.getParticipants().stream()
                        .filter(p -> p.getRole() == ParticipantRole.REFEREE)
                        .anyMatch(p -> p.getJoinStatus() == JoinStatus.PAID);

                if (refereePaid) {
                    booking.confirm();
                    teamBalancingService.balanceTeams(booking);
                }
            }
        }
    }

    private void autoAssignReferee(Booking booking) {
        java.util.List<Referee> readyReferees = refereeRepository.findEligibleReferees().stream()
                .filter(Referee::getIsReady)
                .collect(java.util.stream.Collectors.toList());

        if (readyReferees.isEmpty()) {
            return; // No referee available right now, match stays pending
        }

        BigDecimal depositAmount = booking.getTotalCost().getAmount()
                .multiply(DEPOSIT_PERCENTAGE)
                .setScale(0, RoundingMode.HALF_UP);
        Money requiredDeposit = new Money(depositAmount, "VND");

        for (Referee ref : readyReferees) {
            try {
                String description = "Referee deposit - Auto Assigned Booking #" + booking.getId();
                payWithWalletUseCase.execute(
                        ref.getUserId(),
                        depositAmount,
                        booking.getId(),
                        description
                );

                BookingParticipant refBp = BookingParticipant.builder()
                        .bookingId(booking.getId())
                        .userId(ref.getUserId())
                        .role(ParticipantRole.REFEREE)
                        .joinStatus(JoinStatus.PAID)
                        .depositAmount(requiredDeposit)
                        .refundAmount(new Money(BigDecimal.ZERO, "VND"))
                        .isMatchHost(false)
                        .build();
                booking.addParticipant(refBp);
                assignRefereeToRankedMatch(booking.getId(), ref.getUserId());
                break; // Successfully assigned
            } catch (Exception e) {
                // Insufficient balance, try next
            }
        }
    }
}
