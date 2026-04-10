package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.EloHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EloHistoryJpaRepository extends JpaRepository<EloHistoryEntity, Long> {
    List<EloHistoryEntity> findByUserId(Long userId);
    List<EloHistoryEntity> findByRankedMatchId(Long rankedMatchId);
}

