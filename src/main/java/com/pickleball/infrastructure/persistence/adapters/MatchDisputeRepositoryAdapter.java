package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.enums.DisputeStatus;
import com.pickleball.domain.repositories.MatchDisputeRepository;
import com.pickleball.infrastructure.persistence.mappers.MatchDisputeMapper;
import com.pickleball.infrastructure.persistence.repositories.MatchDisputeJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MatchDisputeRepositoryAdapter implements MatchDisputeRepository {

    private final MatchDisputeJpaRepository jpaRepository;

    public MatchDisputeRepositoryAdapter(MatchDisputeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MatchDispute save(MatchDispute dispute) {
        var entity = MatchDisputeMapper.toEntity(dispute);
        var saved = jpaRepository.save(entity);
        return MatchDisputeMapper.toDomain(saved);
    }

    @Override
    public Optional<MatchDispute> findById(Long id) {
        return jpaRepository.findById(id).map(MatchDisputeMapper::toDomain);
    }

    @Override
    public List<MatchDispute> findByRankedMatchId(Long rankedMatchId) {
        return jpaRepository.findByRankedMatchId(rankedMatchId).stream()
                .map(MatchDisputeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDispute> findByStatus(DisputeStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(MatchDisputeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDispute> findByReportingPlayerId(Long playerId) {
        return jpaRepository.findByReportingPlayerId(playerId).stream()
                .map(MatchDisputeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDispute> findExpiredAwaitingEvidence(LocalDateTime now) {
        return jpaRepository.findExpiredAwaitingEvidence(now).stream()
                .map(MatchDisputeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDispute> findAll() {
        return jpaRepository.findAll().stream()
                .map(MatchDisputeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDispute> findByRefereeId(Long refereeId) {
        return jpaRepository.findByRefereeId(refereeId).stream()
                .map(MatchDisputeMapper::toDomain)
                .collect(Collectors.toList());
    }
}
