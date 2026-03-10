package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.infrastructure.persistence.entities.MatchDisputeEntity;

public class MatchDisputeMapper {

    public static MatchDispute toDomain(MatchDisputeEntity entity) {
        if (entity == null) return null;
        return MatchDispute.builder()
                .id(entity.getId())
                .rankedMatchId(entity.getRankedMatchId())
                .reportingPlayerId(entity.getReportingPlayerId())
                .reason(entity.getReason())
                .evidence(entity.getEvidence())
                .refereeEvidenceUrl(entity.getRefereeEvidenceUrl())
                .refereeResponse(entity.getRefereeResponse())
                .status(entity.getStatus())
                .evidenceDeadline(entity.getEvidenceDeadline())
                .resolvedByAdminId(entity.getResolvedByAdminId())
                .adminDecision(entity.getAdminDecision())
                .decisionType(entity.getDecisionType())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }

    public static MatchDisputeEntity toEntity(MatchDispute domain) {
        if (domain == null) return null;
        return MatchDisputeEntity.builder()
                .id(domain.getId())
                .rankedMatchId(domain.getRankedMatchId())
                .reportingPlayerId(domain.getReportingPlayerId())
                .reason(domain.getReason())
                .evidence(domain.getEvidence())
                .refereeEvidenceUrl(domain.getRefereeEvidenceUrl())
                .refereeResponse(domain.getRefereeResponse())
                .status(domain.getStatus())
                .evidenceDeadline(domain.getEvidenceDeadline())
                .resolvedByAdminId(domain.getResolvedByAdminId())
                .adminDecision(domain.getAdminDecision())
                .decisionType(domain.getDecisionType())
                .resolvedAt(domain.getResolvedAt())
                .build();
    }
}
