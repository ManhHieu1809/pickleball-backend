package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.BookingParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingParticipantJpaRepository extends JpaRepository<BookingParticipantEntity, Long> {
    List<BookingParticipantEntity> findByBookingId(Long bookingId);
    Optional<BookingParticipantEntity> findByBookingIdAndUserId(Long bookingId, Long userId);
    List<BookingParticipantEntity> findByUserId(Long userId);
    List<BookingParticipantEntity> findByBookingIdAndRole(Long bookingId, String role);

    /**
     * Tìm các user IDs đã chơi cùng userId trong N trận casual/ranked gần nhất (đã COMPLETED).
     * Dùng cho anti-repetition filter.
     */
    @Query(value = "SELECT DISTINCT bp2.user_id FROM booking_participants bp1 " +
            "JOIN bookings b ON bp1.booking_id = b.id " +
            "JOIN booking_participants bp2 ON b.id = bp2.booking_id " +
            "WHERE bp1.user_id = :userId " +
            "AND bp2.user_id != :userId " +
            "AND b.booking_type IN ('CASUAL', 'RANKED') " +
            "AND b.status = 'COMPLETED' " +
            "AND b.id IN (SELECT temp.id FROM (SELECT sub_b.id FROM bookings sub_b " +
            "   JOIN booking_participants sub_bp ON sub_b.id = sub_bp.booking_id " +
            "   WHERE sub_bp.user_id = :userId " +
            "   AND sub_b.booking_type IN ('CASUAL', 'RANKED') " +
            "   AND sub_b.status = 'COMPLETED' " +
            "   ORDER BY sub_b.start_time DESC LIMIT :lastNMatches) AS temp)",
            nativeQuery = true)
    List<Long> findRecentOpponentUserIds(@Param("userId") Long userId, @Param("lastNMatches") int lastNMatches);
}