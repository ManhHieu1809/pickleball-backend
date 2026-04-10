package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.CheckIn;
import com.pickleball.infrastructure.persistence.entities.CheckInEntity;

public class CheckInMapper {
    public static CheckIn toDomain(CheckInEntity entity) {
        if (entity == null) return null;
        return CheckIn.builder()
                .id(entity.getId())
                .bookingId(entity.getBookingId())
                .userId(entity.getUserId())
                .checkInMethod(entity.getCheckInMethod())
                .checkInTime(entity.getCheckInTime())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }

    public static CheckInEntity toEntity(CheckIn domain) {
        if (domain == null) return null;
        return CheckInEntity.builder()
                .id(domain.getId())
                .bookingId(domain.getBookingId())
                .userId(domain.getUserId())
                .checkInMethod(domain.getCheckInMethod())
                .checkInTime(domain.getCheckInTime())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .build();
    }
}
