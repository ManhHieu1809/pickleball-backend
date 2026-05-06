package com.pickleball.application.usecases.matchmaking;

import com.pickleball.domain.repositories.MatchmakingTicketRepository;
import org.springframework.transaction.annotation.Transactional;

public class LeaveMatchmakingQueueUseCase {

    private final MatchmakingTicketRepository matchmakingTicketRepository;

    public LeaveMatchmakingQueueUseCase(MatchmakingTicketRepository matchmakingTicketRepository) {
        this.matchmakingTicketRepository = matchmakingTicketRepository;
    }

    @Transactional
    public void execute(Long userId) {
        matchmakingTicketRepository.deactivateTicket(userId);
    }
}

