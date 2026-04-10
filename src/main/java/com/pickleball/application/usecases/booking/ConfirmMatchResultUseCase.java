package com.pickleball.application.usecases.booking;

import com.pickleball.application.services.SettlementService;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ConfirmMatchResultUseCase {
    private final RankedMatchRepository rankedMatchRepository;
    private final BookingRepository bookingRepository;
    private final UpdateEloUseCase updateEloUseCase;
    private final SettlementService settlementService;

    @Transactional
    public void execute(Long bookingId, Long playerUserId, Boolean accepted) {
        RankedMatch match = rankedMatchRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Ranked match not found for booking ID: " + bookingId));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        // Validate player is a participant (PLAYER role)
        boolean isParticipant = booking.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(playerUserId) && p.getRole() == ParticipantRole.PLAYER);

        if (!isParticipant) {
            throw new IllegalArgumentException("User is not a player participant in this match");
        }

        if (Boolean.TRUE.equals(accepted)) {
            match.confirm(playerUserId);
        } else {
            match.dispute();
        }
        
        // If match is now CONFIRMED (all players agreed), finalize the flow
        if (match.getStatus() == MatchStatus.CONFIRMED) {
            // 1. Update Elo
            updateEloUseCase.execute(bookingId);
            
            // 2. Mark Booking as COMPLETED
            booking.setStatus(BookingStatus.COMPLETED);
            bookingRepository.save(booking);
            
            // 3. Process Settlement
            settlementService.processSettlement(bookingId);
        }

        rankedMatchRepository.save(match);
    }
}
