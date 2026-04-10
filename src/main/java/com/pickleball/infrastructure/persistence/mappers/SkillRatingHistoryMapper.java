package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.SkillRatingHistory;
import com.pickleball.infrastructure.persistence.entities.SkillRatingHistoryEntity;

public class SkillRatingHistoryMapper {

    public static SkillRatingHistory toDomain(SkillRatingHistoryEntity entity) {
        if (entity == null) return null;
        return SkillRatingHistory.builder()
                .id(entity.getId())
                .playerId(entity.getPlayerId())
                .matchId(entity.getMatchId())
                .seasonId(entity.getSeasonId())
                .muBefore(entity.getMuBefore())
                .sigmaBefore(entity.getSigmaBefore())
                .muAfter(entity.getMuAfter())
                .sigmaAfter(entity.getSigmaAfter())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static SkillRatingHistoryEntity toEntity(SkillRatingHistory domain) {
        if (domain == null) return null;
        return SkillRatingHistoryEntity.builder()
                .id(domain.getId())
                .playerId(domain.getPlayerId())
                .matchId(domain.getMatchId())
                .seasonId(domain.getSeasonId())
                .muBefore(domain.getMuBefore())
                .sigmaBefore(domain.getSigmaBefore())
                .muAfter(domain.getMuAfter())
                .sigmaAfter(domain.getSigmaAfter())
                .build();
    }
}

