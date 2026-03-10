package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingEntity, Long> {
        List<BookingEntity> findByCourtIdAndStartTimeBetween(Long courtId, LocalDateTime start, LocalDateTime end);

        List<BookingEntity> findByCreatedByPlayerIdOrderByStartTimeDesc(Long playerId);

        List<BookingEntity> findByStatus(BookingStatus status);

        List<BookingEntity> findByBookingTypeAndStatus(BookingType bookingType, BookingStatus status);

        @Query("SELECT b FROM BookingEntity b WHERE b.courtId = :courtId AND " +
                        "b.status != 'CANCELLED' AND " +
                        "(b.startTime < :endTime AND b.endTime > :startTime)")
        List<BookingEntity> findConflictingBookings(@Param("courtId") Long courtId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // Dashboard queries
        long countByBookingType(BookingType bookingType);

        long countByStartTimeBetween(LocalDateTime start, LocalDateTime end);

        @Query("SELECT b FROM BookingEntity b ORDER BY b.createdAt DESC")
        List<BookingEntity> findRecentBookings(Pageable pageable);

        // Admin Booking Management queries
        @Query("SELECT b FROM BookingEntity b " +
                        "LEFT JOIN b.court c " +
                        "LEFT JOIN c.venue v " +
                        "WHERE (:search IS NULL OR " +
                        "  CAST(b.id AS string) LIKE %:search% OR " +
                        "  c.courtName LIKE %:search% OR " +
                        "  v.name LIKE %:search%) AND " +
                        "(:status IS NULL OR b.status = :status) AND " +
                        "(:type IS NULL OR b.bookingType = :type)")
        org.springframework.data.domain.Page<BookingEntity> searchBookings(
                        @Param("search") String search,
                        @Param("status") BookingStatus status,
                        @Param("type") BookingType type,
                        Pageable pageable);

        long countByStatus(BookingStatus status);

        // Tìm casual bookings PENDING đã quá giờ start
        @Query("SELECT b FROM BookingEntity b WHERE b.bookingType = 'CASUAL' AND b.status = 'PENDING' AND b.startTime < :now")
        List<BookingEntity> findExpiredPendingCasual(@Param("now") LocalDateTime now);
}
