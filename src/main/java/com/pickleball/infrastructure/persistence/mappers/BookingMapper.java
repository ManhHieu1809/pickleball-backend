package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.valueobjects.Money;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import com.pickleball.infrastructure.persistence.entities.BookingParticipantEntity;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

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

        if (domainBooking.getParticipants() != null) {
            java.util.List<BookingParticipantEntity> participantEntities = new java.util.ArrayList<>();
            for (com.pickleball.domain.entities.BookingParticipant bp : domainBooking.getParticipants()) {
                BookingParticipantEntity bpe = new BookingParticipantEntity();
                bpe.setId(bp.getId());
                bpe.setBookingId(bp.getBookingId());
                bpe.setBooking(entity);
                bpe.setUserId(bp.getUserId());
                bpe.setRole(bp.getRole());
                bpe.setTeam(bp.getTeam());
                bpe.setJoinStatus(bp.getJoinStatus());
                if (bp.getDepositAmount() != null) bpe.setDepositAmount(bp.getDepositAmount().getAmount());
                if (bp.getActualPaymentAmount() != null) bpe.setActualPaymentAmount(bp.getActualPaymentAmount().getAmount());
                if (bp.getRefundAmount() != null) bpe.setRefundAmount(bp.getRefundAmount().getAmount());
                bpe.setIsMatchHost(bp.isMatchHost());
                participantEntities.add(bpe);
            }
            entity.setParticipants(participantEntities);
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

        if (entity.getParticipants() != null) {
            java.util.List<com.pickleball.domain.entities.BookingParticipant> participants = new java.util.ArrayList<>();
            for (BookingParticipantEntity bpe : entity.getParticipants()) {
                com.pickleball.domain.entities.BookingParticipant bp = new com.pickleball.domain.entities.BookingParticipant();
                bp.setId(bpe.getId());
                bp.setBookingId(bpe.getBookingId());
                bp.setUserId(bpe.getUserId());
                bp.setRole(bpe.getRole());
                bp.setTeam(bpe.getTeam());
                bp.setJoinStatus(bpe.getJoinStatus());
                if (bpe.getDepositAmount() != null) bp.setDepositAmount(new com.pickleball.domain.valueobjects.Money(bpe.getDepositAmount(), "VND"));
                if (bpe.getActualPaymentAmount() != null) bp.setActualPaymentAmount(new com.pickleball.domain.valueobjects.Money(bpe.getActualPaymentAmount(), "VND"));
                if (bpe.getRefundAmount() != null) bp.setRefundAmount(new com.pickleball.domain.valueobjects.Money(bpe.getRefundAmount(), "VND"));
                bp.setMatchHost(bpe.getIsMatchHost() != null ? bpe.getIsMatchHost() : false);
                participants.add(bp);
            }
            domainBooking.setParticipants(participants);
        }

        return domainBooking;
    }
}