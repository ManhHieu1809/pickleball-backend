package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.infrastructure.persistence.entities.RankedMatchEntity;

public class RankedMatchMapper {

    public static RankedMatch toDomain(RankedMatchEntity entity) {
        if (entity == null) return null;
        return RankedMatch.builder()
                .id(entity.getId())
                .bookingId(entity.getBookingId())
                .refereeId(entity.getRefereeId())
                .seasonId(entity.getSeasonId())
                .status(entity.getStatus())
                .teamAScore(entity.getTeamAScore())
                .teamBScore(entity.getTeamBScore())
                .winningTeam(entity.getWinningTeam())
                .submittedAt(entity.getSubmittedAt())
                .confirmedAt(entity.getConfirmedAt())
                .confirmedPlayerIds(entity.getConfirmedPlayerIds())
                .build();
    }

    public static RankedMatchEntity toEntity(RankedMatch domain) {
        if (domain == null) return null;
        return RankedMatchEntity.builder()
                .id(domain.getId())
                .bookingId(domain.getBookingId())
                .refereeId(domain.getRefereeId())
                .seasonId(domain.getSeasonId())
                .status(domain.getStatus())
                .teamAScore(domain.getTeamAScore())
                .teamBScore(domain.getTeamBScore())
                .winningTeam(domain.getWinningTeam())
                .submittedAt(domain.getSubmittedAt())
                .confirmedAt(domain.getConfirmedAt())
                .confirmedPlayerIds(domain.getConfirmedPlayerIds())
                .build();
    }
}
