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
import java.util.Optional;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingEntity, Long> {
        List<BookingEntity> findByCourtIdAndStartTimeBetween(Long courtId, LocalDateTime start, LocalDateTime end);

        List<BookingEntity> findByCreatedByPlayerIdOrderByStartTimeDesc(Long playerId);

        List<BookingEntity> findByCreatedByStaffIdOrderByStartTimeDesc(Long staffId);

        @Query("SELECT b FROM BookingEntity b JOIN b.court c JOIN c.venue v WHERE v.ownerId = :ownerId ORDER BY b.startTime DESC")
        List<BookingEntity> findByOwnerId(@Param("ownerId") Long ownerId);

        @Query("SELECT b FROM BookingEntity b JOIN b.court c WHERE c.venueId = :venueId ORDER BY b.startTime DESC")
        List<BookingEntity> findByVenueId(@Param("venueId") Long venueId);

        List<BookingEntity> findByStatus(BookingStatus status);

        List<BookingEntity> findByBookingTypeAndStatus(BookingType bookingType, BookingStatus status);

        @Query("SELECT b FROM BookingEntity b WHERE b.courtId = :courtId AND " +
                        "b.status != 'CANCELLED' AND " +
                        "(b.startTime < :endTime AND b.endTime > :startTime)")
        List<BookingEntity> findConflictingBookings(@Param("courtId") Long courtId,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // ✅ FIX: Load booking với participants trong 1 query để tránh LazyInitializationException
        @Query("SELECT DISTINCT b FROM BookingEntity b LEFT JOIN FETCH b.participants WHERE b.id = :id")
        Optional<BookingEntity> findByIdWithParticipants(@Param("id") Long id);

        // ✅ FIX: Load danh sách booking với participants (cho getAvailableRankedMatches, etc.)
        @Query("SELECT DISTINCT b FROM BookingEntity b LEFT JOIN FETCH b.participants " +
               "WHERE b.bookingType = :bookingType AND b.status = :status")
        List<BookingEntity> findByBookingTypeAndStatusWithParticipants(
                        @Param("bookingType") BookingType bookingType,
                        @Param("status") BookingStatus status);

        // Dashboard queries
        long countByBookingType(BookingType bookingType);

        long countByStartTimeBetween(LocalDateTime start, LocalDateTime end);

        List<BookingEntity> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

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

        // Tìm ranked bookings PENDING quá hạn thanh toán (2 phút)
        @Query("SELECT b FROM BookingEntity b WHERE b.bookingType = 'RANKED' AND b.status = 'PENDING' AND b.createdAt < :timeout")
        List<BookingEntity> findExpiredPendingRanked(@Param("timeout") LocalDateTime timeout);

        // Tìm ranked bookings CONFIRMED (đã chốt người) nhưng quá giờ start 10 phút để xử lý check-in
        @Query("SELECT DISTINCT b FROM BookingEntity b LEFT JOIN FETCH b.participants WHERE b.bookingType = 'RANKED' AND b.status = 'CONFIRMED' AND b.startTime < :timeThreshold")
        List<BookingEntity> findExpiredRankedNoShows(@Param("timeThreshold") LocalDateTime timeThreshold);

        @Query("SELECT b FROM BookingEntity b JOIN b.participants bp WHERE bp.userId = :userId AND b.status IN ('PENDING', 'CONFIRMED') AND b.bookingType = 'RANKED' ORDER BY b.createdAt DESC")
        List<BookingEntity> findActiveRankedMatchesByParticipantUserId(@Param("userId") Long userId);

        @Query("SELECT DISTINCT b FROM BookingEntity b JOIN b.participants bp WHERE bp.userId = :userId ORDER BY b.startTime DESC")
        List<BookingEntity> findByParticipantUserId(@Param("userId") Long userId);
}
