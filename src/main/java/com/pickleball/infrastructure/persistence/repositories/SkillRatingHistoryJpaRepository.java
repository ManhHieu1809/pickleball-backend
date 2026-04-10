package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.SkillRatingHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillRatingHistoryJpaRepository extends JpaRepository<SkillRatingHistoryEntity, Long> {
    List<SkillRatingHistoryEntity> findByPlayerId(Long playerId);
}

