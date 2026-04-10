package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.SkillRatingHistory;
import com.pickleball.domain.repositories.SkillRatingHistoryRepository;
import com.pickleball.infrastructure.persistence.entities.SkillRatingHistoryEntity;
import com.pickleball.infrastructure.persistence.mappers.SkillRatingHistoryMapper;
import com.pickleball.infrastructure.persistence.repositories.SkillRatingHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SkillRatingHistoryRepositoryAdapter implements SkillRatingHistoryRepository {

    private final SkillRatingHistoryJpaRepository jpaRepository;

    @Override
    public SkillRatingHistory save(SkillRatingHistory skillRatingHistory) {
        SkillRatingHistoryEntity entity = SkillRatingHistoryMapper.toEntity(skillRatingHistory);
        SkillRatingHistoryEntity saved = jpaRepository.save(entity);
        return SkillRatingHistoryMapper.toDomain(saved);
    }

    @Override
    public List<SkillRatingHistory> findByPlayerId(Long playerId) {
        return jpaRepository.findByPlayerId(playerId).stream()
                .map(SkillRatingHistoryMapper::toDomain)
                .collect(Collectors.toList());
    }
}

