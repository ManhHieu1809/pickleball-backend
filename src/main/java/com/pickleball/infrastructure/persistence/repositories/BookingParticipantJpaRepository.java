package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.BookingParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingParticipantJpaRepository extends JpaRepository<BookingParticipantEntity, Long> {
    List<BookingParticipantEntity> findByBookingId(Long bookingId);
    Optional<BookingParticipantEntity> findByBookingIdAndUserId(Long bookingId, Long userId);
    List<BookingParticipantEntity> findByUserId(Long userId);
    List<BookingParticipantEntity> findByBookingIdAndRole(Long bookingId, String role);
}