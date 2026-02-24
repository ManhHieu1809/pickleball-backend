package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.infrastructure.persistence.entities.RoleRequestEntity;
import org.springframework.stereotype.Component;

@Component
public class RoleRequestMapper {

    public RoleRequestEntity toEntity(RoleRequest domain) {
        if (domain == null) {
            return null;
        }

        return RoleRequestEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .requestType(domain.getRequestType())
                .venueId(domain.getVenueId())
                .legalInfo(domain.getLegalInfo())
                .testScore(domain.getTestScore())
                .status(domain.getStatus())
                .submittedAt(domain.getSubmittedAt())
                .processedByAdminId(domain.getProcessedByAdminId())
                .processedAt(domain.getProcessedAt())
                .notes(domain.getNotes())
                .build();
    }

    public RoleRequest toDomain(RoleRequestEntity entity) {
        if (entity == null) {
            return null;
        }

        return RoleRequest.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .requestType(entity.getRequestType())
                .venueId(entity.getVenueId())
                .legalInfo(entity.getLegalInfo())
                .testScore(entity.getTestScore())
                .status(entity.getStatus())
                .submittedAt(entity.getSubmittedAt())
                .processedByAdminId(entity.getProcessedByAdminId())
                .processedAt(entity.getProcessedAt())
                .notes(entity.getNotes())
                .build();
    }
}

