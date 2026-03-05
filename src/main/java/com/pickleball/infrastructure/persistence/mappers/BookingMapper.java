package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.valueobjects.Money;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BookingMapper {

    public BookingEntity toEntity(Booking domainBooking) {
        if (domainBooking == null) {
            return null;
        }

        BookingEntity entity = new BookingEntity();
        entity.setId(domainBooking.getId());
        entity.setCourtId(domainBooking.getCourtId());
        entity.setStartTime(domainBooking.getStartTime());
        entity.setEndTime(domainBooking.getEndTime());
        entity.setBookingType(domainBooking.getBookingType());
        entity.setStatus(domainBooking.getStatus());
        entity.setCreatedByPlayerId(domainBooking.getCreatedByPlayerId());
        entity.setCreatedByStaffId(domainBooking.getCreatedByStaffId());
        entity.setCreatedAt(domainBooking.getCreatedAt());
        entity.setNotes(domainBooking.getNotes());

        // Convert Money objects to BigDecimal
        if (domainBooking.getVenueFee() != null) {
            entity.setVenueFee(domainBooking.getVenueFee().getAmount());
        }
        if (domainBooking.getRefereeFee() != null) {
            entity.setRefereeFee(domainBooking.getRefereeFee().getAmount());
        }
        if (domainBooking.getPlatformFee() != null) {
            entity.setPlatformFee(domainBooking.getPlatformFee().getAmount());
        }
        if (domainBooking.getTotalCost() != null) {
            entity.setTotalCost(domainBooking.getTotalCost().getAmount());
        }

        return entity;
    }

    public Booking toDomain(BookingEntity entity) {
        if (entity == null) {
            return null;
        }

        Booking domainBooking = Booking.builder()
                .id(entity.getId())
                .courtId(entity.getCourtId())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .bookingType(entity.getBookingType())
                .status(entity.getStatus())
                .createdByPlayerId(entity.getCreatedByPlayerId())
                .createdByStaffId(entity.getCreatedByStaffId())
                .venueFee(entity.getVenueFee() != null ? new Money(entity.getVenueFee(), "VND") : null)
                .refereeFee(entity.getRefereeFee() != null ? new Money(entity.getRefereeFee(), "VND") : null)
                .platformFee(entity.getPlatformFee() != null ? new Money(entity.getPlatformFee(), "VND") : null)
                .totalCost(entity.getTotalCost() != null ? new Money(entity.getTotalCost(), "VND") : null)
                .createdAt(entity.getCreatedAt())
                .notes(entity.getNotes())
                .build();

        return domainBooking;
    }
}