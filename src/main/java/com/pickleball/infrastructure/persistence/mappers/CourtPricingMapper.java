package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.valueobjects.Money;
import com.pickleball.infrastructure.persistence.entities.CourtPricingEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;

@Component
public class CourtPricingMapper {

    public CourtPricingEntity toEntity(CourtPricing domainPricing) {
        if (domainPricing == null) {
            return null;
        }

        CourtPricingEntity entity = CourtPricingEntity.builder()
                .id(domainPricing.getId())
                .courtId(domainPricing.getCourtId())
                .startTime(domainPricing.getStartTime())
                .endTime(domainPricing.getEndTime())
                .pricePerHour(domainPricing.getPricePerHour().getAmount())
                .build();

        if (domainPricing.getDayOfWeek() != null) {
            entity.setDayOfWeek(domainPricing.getDayOfWeek().getValue());
        }

        return entity;
    }

    public CourtPricing toDomain(CourtPricingEntity entity) {
        if (entity == null) {
            return null;
        }

        DayOfWeek dayOfWeek = null;
        if (entity.getDayOfWeek() != null) {
            dayOfWeek = DayOfWeek.of(entity.getDayOfWeek());
        }

        Money pricePerHour = new Money(entity.getPricePerHour(), "VND");

        CourtPricing pricing = CourtPricing.builder()
                .id(entity.getId())
                .courtId(entity.getCourtId())
                .dayOfWeek(dayOfWeek)
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .pricePerHour(pricePerHour)
                .build();

        return pricing;
    }
}