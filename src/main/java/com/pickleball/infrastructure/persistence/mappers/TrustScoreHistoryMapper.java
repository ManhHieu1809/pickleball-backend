package com.pickleball.infrastructure.persistence.mappers;
import com.pickleball.domain.entities.TrustScoreHistory;
import com.pickleball.infrastructure.persistence.entities.TrustScoreHistoryEntity;
public class TrustScoreHistoryMapper {
    public static TrustScoreHistory toDomain(TrustScoreHistoryEntity entity) {
        if (entity == null) return null;
        return TrustScoreHistory.builder()
                .id(entity.getId())
                .refereeId(entity.getRefereeId())
                .oldScore(entity.getOldScore())
                .newScore(entity.getNewScore())
                .reason(entity.getReason())
                .changedAt(entity.getChangedAt())
                .associatedMatchId(entity.getAssociatedMatchId())
                .build();
    }
    public static TrustScoreHistoryEntity toEntity(TrustScoreHistory domain) {
        if (domain == null) return null;
        return TrustScoreHistoryEntity.builder()
                .id(domain.getId())
                .refereeId(domain.getRefereeId())
                .oldScore(domain.getOldScore())
                .newScore(domain.getNewScore())
                .reason(domain.getReason())
                .changedAt(domain.getChangedAt())
                .associatedMatchId(domain.getAssociatedMatchId())
                .build();
    }
}
