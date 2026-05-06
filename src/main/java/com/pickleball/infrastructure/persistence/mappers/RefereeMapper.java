package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.Referee;
import com.pickleball.infrastructure.persistence.entities.RefereeEntity;
import com.pickleball.infrastructure.persistence.entities.UserEntity;

public class RefereeMapper {

    public static Referee toDomain(RefereeEntity entity) {
        if (entity == null) return null;
        return Referee.builder()
                .userId(entity.getUserId())
                .testPassed(entity.getTestPassed())
                .testScore(entity.getTestScore())
                .refereeType(entity.getRefereeType())
                .worksAtVenueId(entity.getWorksAtVenueId())
                .approvedByAdminId(entity.getApprovedByAdminId())
                .approvedAt(entity.getApprovedAt())
                .isActive(entity.getIsActive())
                .isReady(entity.getIsReady())
                .trustScore(entity.getTrustScore())
                .totalMatchesRefereed(entity.getTotalMatchesRefereed())
                .build();
    }

    public static RefereeEntity toEntity(Referee domain) {
        if (domain == null) return null;
        return RefereeEntity.builder()
                .userId(domain.getUserId())
                .user(UserEntity.builder().id(domain.getUserId()).build())
                .testPassed(domain.getTestPassed())
                .testScore(domain.getTestScore())
                .refereeType(domain.getRefereeType())
                .worksAtVenueId(domain.getWorksAtVenueId())
                .approvedByAdminId(domain.getApprovedByAdminId())
                .approvedAt(domain.getApprovedAt())
                .isActive(domain.getIsActive())
                .isReady(domain.getIsReady())
                .trustScore(domain.getTrustScore())
                .totalMatchesRefereed(domain.getTotalMatchesRefereed())
                .build();
    }
}
