package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.EloHistory;
import com.pickleball.infrastructure.persistence.entities.EloHistoryEntity;

public class EloHistoryMapper {

    public static EloHistory toDomain(EloHistoryEntity entity) {
        if (entity == null) return null;
        return EloHistory.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .rankedMatchId(entity.getRankedMatchId())
                .seasonId(entity.getSeasonId())
                .eloBefore(entity.getEloBefore())
                .eloChange(entity.getEloChange())
                .eloAfter(entity.getEloAfter())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static EloHistoryEntity toEntity(EloHistory domain) {
        if (domain == null) return null;
        return EloHistoryEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .rankedMatchId(domain.getRankedMatchId())
                .seasonId(domain.getSeasonId())
                .eloBefore(domain.getEloBefore())
                .eloChange(domain.getEloChange())
                .eloAfter(domain.getEloAfter())
                .build();
    }
}

