package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.VenueOwner;
import com.pickleball.infrastructure.persistence.entities.VenueOwnerEntity;
import org.springframework.stereotype.Component;

@Component
public class VenueOwnerMapper {

    public VenueOwnerEntity toEntity(VenueOwner domain) {
        if (domain == null) {
            return null;
        }

        return VenueOwnerEntity.builder()
                .userId(domain.getUserId())
                .taxCode(domain.getTaxCode())
                .bankAccountNumber(domain.getBankAccountNumber())
                .bankName(domain.getBankName())
                .build();
    }

    public VenueOwner toDomain(VenueOwnerEntity entity) {
        if (entity == null) {
            return null;
        }

        return VenueOwner.builder()
                .userId(entity.getUserId())
                .taxCode(entity.getTaxCode())
                .bankAccountNumber(entity.getBankAccountNumber())
                .bankName(entity.getBankName())
                .build();
    }
}

