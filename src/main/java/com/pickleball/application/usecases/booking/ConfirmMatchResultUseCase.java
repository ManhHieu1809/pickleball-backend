package com.pickleball.application.usecases.booking;

import com.pickleball.application.services.SettlementService;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.DisputeStatus;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.MatchDisputeRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ConfirmMatchResultUseCase {
    private final RankedMatchRepository rankedMatchRepository;
    private final BookingRepository bookingRepository;
    private final MatchDisputeRepository matchDisputeRepository;
    private final UpdateEloUseCase updateEloUseCase;
    private final SettlementService settlementService;

    @Transactional
    public void execute(Long bookingId, Long playerUserId, Boolean accepted) {
        RankedMatch match = rankedMatchRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Ranked match not found for booking ID: " + bookingId));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        boolean isParticipant = booking.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(playerUserId)
                        && (p.getRole() == ParticipantRole.PLAYER || p.getRole() == ParticipantRole.HOST));

        if (!isParticipant) {
            throw new IllegalArgumentException("User is not a player participant in this match");
        }

        if (Boolean.TRUE.equals(accepted)) {
            match.confirm(playerUserId);
        } else {
            match.dispute();

            // Auto-create a dispute record when a player rejects the match result
            MatchDispute dispute = MatchDispute.builder()
                    .rankedMatchId(match.getId())
                    .reportingPlayerId(playerUserId)
                    .reason("Player rejected the match result during confirmation")
                    .status(DisputeStatus.OPEN)
                    .build();
            matchDisputeRepository.save(dispute);
        }

        // Save early so that updateEloUseCase sees the correct status from the repository adapter
        rankedMatchRepository.save(match);

        if (match.getStatus() == MatchStatus.CONFIRMED) {
            updateEloUseCase.execute(bookingId);

            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);

            settlementService.processSettlement(bookingId);
        }
    }
}
