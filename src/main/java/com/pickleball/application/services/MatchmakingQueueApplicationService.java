package com.pickleball.application.services;

import com.pickleball.application.dtos.MatchmakingTicketDTO;
import com.pickleball.application.dtos.requests.JoinMatchmakingQueueRequest;
import com.pickleball.application.usecases.matchmaking.JoinMatchmakingQueueUseCase;
import com.pickleball.application.usecases.matchmaking.LeaveMatchmakingQueueUseCase;
import com.pickleball.domain.entities.MatchmakingTicket;
import com.pickleball.domain.repositories.MatchmakingTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchmakingQueueApplicationService {

    private final JoinMatchmakingQueueUseCase joinUseCase;
    private final LeaveMatchmakingQueueUseCase leaveUseCase;
    private final MatchmakingTicketRepository repository;
    private final com.pickleball.domain.repositories.BookingRepository bookingRepository;
    private final com.pickleball.domain.repositories.RankedMatchRepository rankedMatchRepository;

    @Transactional
    public MatchmakingTicketDTO joinQueue(JoinMatchmakingQueueRequest request) {
        MatchmakingTicket ticket = joinUseCase.execute(
                request.getUserId(),
                request.getRole(),
                request.getLatitude(),
                request.getLongitude()
        );
        return convertToDTO(ticket);
    }

    @Transactional
    public void leaveQueue(Long userId) {
        leaveUseCase.execute(userId);

        var activeMatches = bookingRepository.findActiveRankedMatchesByUserId(userId);
        if (activeMatches != null && !activeMatches.isEmpty()) {
            for (var match : activeMatches) {
                if (match.getStatus() == com.pickleball.domain.enums.BookingStatus.PENDING) {
                    match.cancel();
                    bookingRepository.save(match);

                    rankedMatchRepository.findByBookingId(match.getId()).ifPresent(rm -> {
                        rm.setStatus(com.pickleball.domain.enums.MatchStatus.CANCELLED);
                        rankedMatchRepository.save(rm);
                    });
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public MatchmakingTicketDTO getMyStatus(Long userId) {
        Optional<MatchmakingTicket> activeTicket = repository.findByUserIdAndIsActiveTrue(userId);
        if (activeTicket.isPresent()) {
            return convertToDTO(activeTicket.get());
        }

        // If not in queue, check if they were recently put into a PENDING/CONFIRMED ranked match
        var activeMatches = bookingRepository.findActiveRankedMatchesByUserId(userId);
        if (activeMatches != null && !activeMatches.isEmpty()) {
            var activeMatch = activeMatches.get(0);
            return MatchmakingTicketDTO.builder()
                .userId(userId)
                .isActive(false)
                .matchStatus(activeMatch.getStatus().name())
                .matchedBookingId(activeMatch.getId())
                .build();
        }

        return null;
    }

    private MatchmakingTicketDTO convertToDTO(MatchmakingTicket ticket) {
        if (ticket == null) return null;
        return MatchmakingTicketDTO.builder()
                .id(ticket.getId())
                .userId(ticket.getUserId())
                .role(ticket.getRole())
                .latitude(ticket.getLatitude())
                .longitude(ticket.getLongitude())
                .elo(ticket.getElo())
                .joinedAt(ticket.getJoinedAt())
                .isActive(ticket.getIsActive())
                .build();
    }
}
