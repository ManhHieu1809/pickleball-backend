package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.EloHistory;
import com.pickleball.domain.repositories.EloHistoryRepository;
import com.pickleball.infrastructure.persistence.entities.EloHistoryEntity;
import com.pickleball.infrastructure.persistence.mappers.EloHistoryMapper;
import com.pickleball.infrastructure.persistence.repositories.EloHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EloHistoryRepositoryAdapter implements EloHistoryRepository {

    private final EloHistoryJpaRepository jpaRepository;

    @Override
    public EloHistory save(EloHistory eloHistory) {
        EloHistoryEntity entity = EloHistoryMapper.toEntity(eloHistory);
        EloHistoryEntity saved = jpaRepository.save(entity);
        return EloHistoryMapper.toDomain(saved);
    }

    @Override
    public List<EloHistory> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(EloHistoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<EloHistory> findByRankedMatchId(Long rankedMatchId) {
        return jpaRepository.findByRankedMatchId(rankedMatchId).stream()
                .map(EloHistoryMapper::toDomain)
                .collect(Collectors.toList());
    }
}

