package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.MatchmakingTicket;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.MatchmakingTicketRepository;
import com.pickleball.infrastructure.persistence.entities.MatchmakingTicketEntity;
import com.pickleball.infrastructure.persistence.mappers.MatchmakingTicketMapper;
import com.pickleball.infrastructure.persistence.repositories.MatchmakingTicketJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MatchmakingTicketRepositoryAdapter implements MatchmakingTicketRepository {

    private final MatchmakingTicketJpaRepository jpaRepository;
    private final MatchmakingTicketMapper mapper;

    @Override
    public MatchmakingTicket save(MatchmakingTicket ticket) {
        MatchmakingTicketEntity entity = mapper.toEntity(ticket);
        MatchmakingTicketEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MatchmakingTicket> findByUserIdAndIsActiveTrue(Long userId) {
        return jpaRepository.findByUserIdAndIsActiveTrue(userId)
                .map(mapper::toDomain);
    }

    @Override
    public List<MatchmakingTicket> findActiveTicketsByRoleOrderByJoinedAtAsc(ParticipantRole role) {
        return jpaRepository.findByRoleAndIsActiveTrueOrderByJoinedAtAsc(role).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deactivateTicket(Long userId) {
        jpaRepository.deactivateByUserId(userId);
    }

    @Override
    public void deactivateTickets(List<Long> userIds) {
        if (userIds != null && !userIds.isEmpty()) {
            jpaRepository.deactivateByUserIds(userIds);
        }
    }
}

