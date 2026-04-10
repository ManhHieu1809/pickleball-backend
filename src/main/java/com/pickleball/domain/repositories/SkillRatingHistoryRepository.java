package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.SkillRatingHistory;
import java.util.List;

public interface SkillRatingHistoryRepository {
    SkillRatingHistory save(SkillRatingHistory skillRatingHistory);
    List<SkillRatingHistory> findByPlayerId(Long playerId);
}

