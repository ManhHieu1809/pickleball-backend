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
import com.pickleball.application.usecases.wallet.PayWithWalletUseCase;
import com.pickleball.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class JoinBookingUseCase {
    private final BookingRepository bookingRepository;
    private final PlayerRepository playerRepository;
    private final RefereeRepository refereeRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final PayWithWalletUseCase payWithWalletUseCase;
    private final MatchmakingService matchmakingService;
    private final TeamBalancingService teamBalancingService;

    private static final BigDecimal DEPOSIT_PERCENTAGE = new BigDecimal("0.25");

    public JoinBookingUseCase(BookingRepository bookingRepository,
                              PlayerRepository playerRepository,
                              RefereeRepository refereeRepository,
                              RankedMatchRepository rankedMatchRepository,
                              PayWithWalletUseCase payWithWalletUseCase,
                              MatchmakingService matchmakingService,
                              TeamBalancingService teamBalancingService) {
        this.bookingRepository = bookingRepository;
        this.playerRepository = playerRepository;
        this.refereeRepository = refereeRepository;
        this.rankedMatchRepository = rankedMatchRepository;
        this.payWithWalletUseCase = payWithWalletUseCase;
        this.matchmakingService = matchmakingService;
        this.teamBalancingService = teamBalancingService;
    }


    public JoinResult execute(Long bookingId, Long userId) {
        return execute(bookingId, userId, false);
    }


    public JoinResult execute(Long bookingId, Long userId, boolean asReferee) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Booking is not available to join");
        }

        boolean alreadyJoined = booking.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));
        if (alreadyJoined) {
            throw new IllegalArgumentException("User already joined this booking");
        }

        ParticipantRole role;
        if (asReferee) {
            role = ParticipantRole.REFEREE;
            validateRefereeJoin(booking, userId);
        } else {
            role = ParticipantRole.PLAYER;
            validatePlayerJoin(booking, userId);
        }

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

        JoinStatus joinStatus;
        if (paymentResult.success() && "SUCCESS".equals(paymentResult.status())) {
            joinStatus = JoinStatus.PAID;
        } else if (!paymentResult.success()) {
            throw new IllegalArgumentException("Payment failed: " + paymentResult.message());
        } else {
            joinStatus = JoinStatus.PENDING;
        }

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

        if (asReferee && joinStatus == JoinStatus.PAID) {
            assignRefereeToRankedMatch(booking.getId(), userId);
        }

        autoConfirmIfReady(booking);

        Booking saved = bookingRepository.save(booking);
        return new JoinResult(saved, paymentResult);
    }

    private void validatePlayerJoin(Booking booking, Long userId) {
        if (booking.getBookingType() == BookingType.CASUAL
                || booking.getBookingType() == BookingType.RANKED) {
            validateEloCompatibility(booking, userId);

            long playerCount = booking.getParticipants().stream()
                    .filter(p -> p.getRole() != ParticipantRole.REFEREE)
                    .count();
            if (playerCount >= matchmakingService.getMaxPlayersPerMatch()) {
                throw new IllegalArgumentException("Match is already full (4 players max)");
            }
        }
    }

    private void validateRefereeJoin(Booking booking, Long userId) {
        if (booking.getBookingType() != BookingType.RANKED) {
            throw new IllegalArgumentException("Only ranked matches require a referee");
        }

        boolean hasReferee = booking.getParticipants().stream()
                .anyMatch(p -> p.getRole() == ParticipantRole.REFEREE);
        if (hasReferee) {
            throw new IllegalArgumentException("A referee is already assigned to this match");
        }

        Referee referee = refereeRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a registered referee"));

        if (!referee.isEligibleForMatch()) {
            throw new IllegalArgumentException(
                    "Referee is not eligible for matches (test not passed, inactive, or trust score too low)");
        }
    }

    private Money calculateDeposit(Booking booking, boolean asReferee) {
        if (booking.getBookingType() == BookingType.CASUAL) {
            return calculateCasualDeposit(booking);
        } else if (booking.getBookingType() == BookingType.RANKED) {
            return calculateRankedDeposit(booking);
        }
        return new Money(new BigDecimal("50000"), "VND");
    }

    private Money calculateCasualDeposit(Booking booking) {
        if (booking.getVenueFee() == null) {
            return new Money(new BigDecimal("50000"), "VND");
        }
        BigDecimal deposit = booking.getVenueFee().getAmount()
                .multiply(DEPOSIT_PERCENTAGE)
                .setScale(0, RoundingMode.HALF_UP);
        return new Money(deposit, booking.getVenueFee().getCurrency());
    }

    private Money calculateRankedDeposit(Booking booking) {
        if (booking.getTotalCost() == null) {
            return new Money(new BigDecimal("50000"), "VND");
        }
        BigDecimal deposit = booking.getTotalCost().getAmount()
                .multiply(DEPOSIT_PERCENTAGE)
                .setScale(0, RoundingMode.HALF_UP);
        return new Money(deposit, booking.getTotalCost().getCurrency());
    }

    private void assignRefereeToRankedMatch(Long bookingId, Long refereeUserId) {
        RankedMatch rankedMatch = rankedMatchRepository.findByBookingId(bookingId)
                .orElse(null);
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
            boolean refereePaid = booking.getParticipants().stream()
                    .filter(p -> p.getRole() == ParticipantRole.REFEREE)
                    .anyMatch(p -> p.getJoinStatus() == JoinStatus.PAID);

            if (matchmakingService.isMatchFull((int) paidPlayerCount) && refereePaid) {
                booking.confirm();
                teamBalancingService.balanceTeams(booking);
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