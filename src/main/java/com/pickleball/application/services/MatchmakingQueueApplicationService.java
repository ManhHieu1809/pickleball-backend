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
    }

    @Transactional(readOnly = true)
    public MatchmakingTicketDTO getMyStatus(Long userId) {
        Optional<MatchmakingTicket> activeTicket = repository.findByUserIdAndIsActiveTrue(userId);
        return activeTicket.map(this::convertToDTO).orElse(null);
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

