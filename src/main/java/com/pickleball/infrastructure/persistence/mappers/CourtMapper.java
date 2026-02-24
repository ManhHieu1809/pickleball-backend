package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.Court;
import com.pickleball.infrastructure.persistence.entities.CourtEntity;
import org.springframework.stereotype.Component;

@Component
public class CourtMapper {

    public CourtEntity toEntity(Court domainCourt) {
        if (domainCourt == null) {
            return null;
        }

        return CourtEntity.builder()
                .id(domainCourt.getId())
                .venueId(domainCourt.getVenueId())
                .courtName(domainCourt.getCourtName())
                .isActive(domainCourt.isActive())
                .deactivatedByAdminId(domainCourt.getDeactivatedByAdminId())
                .build();
    }

    public Court toDomain(CourtEntity entity) {
        if (entity == null) {
            return null;
        }

        Court court = Court.builder()
                .id(entity.getId())
                .venueId(entity.getVenueId())
                .courtName(entity.getCourtName())
                .isActive(entity.getIsActive() != null ? entity.getIsActive() : true)
                .deactivatedByAdminId(entity.getDeactivatedByAdminId())
                .build();

        return court;
    }
}