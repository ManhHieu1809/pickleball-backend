package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByCourtIdAndStartTimeBetween(Long courtId, LocalDateTime start, LocalDateTime end);
    List<BookingEntity> findByStatus(BookingStatus status);
    List<BookingEntity> findByBookingTypeAndStatus(BookingType bookingType, BookingStatus status);

    @Query("SELECT b FROM BookingEntity b WHERE b.courtId = :courtId AND " +
            "b.status != 'CANCELLED' AND " +
            "(b.startTime < :endTime AND b.endTime > :startTime)")
    List<BookingEntity> findConflictingBookings(@Param("courtId") Long courtId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);
}