package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.MatchmakingTicket;
import com.pickleball.domain.enums.ParticipantRole;

import java.util.List;
import java.util.Optional;

public interface MatchmakingTicketRepository {
    MatchmakingTicket save(MatchmakingTicket ticket);
    Optional<MatchmakingTicket> findByUserIdAndIsActiveTrue(Long userId);
    List<MatchmakingTicket> findActiveTicketsByUserId(Long userId);
    List<MatchmakingTicket> findActiveTicketsByUserIdOverlapping(Long userId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    List<MatchmakingTicket> findActiveTicketsByRoleOrderByJoinedAtAsc(ParticipantRole role);
    List<MatchmakingTicket> findActiveTicketsByPartyId(Long partyId);
    void deactivateTicket(Long userId);
    void deactivateTickets(List<Long> userIds);
    void deactivateTicketsByPartyId(Long partyId);
}

