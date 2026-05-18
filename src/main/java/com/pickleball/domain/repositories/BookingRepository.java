package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findByPlayerId(Long playerId);

    List<Booking> findByOwnerId(Long ownerId);

    List<Booking> findByVenueId(Long venueId);

    List<Booking> findByStaffId(Long staffId);

    List<Booking> findByCourtIdAndStartTimeBetween(Long courtId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByCourtIdAndDate(Long courtId, LocalDate date);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByBookingTypeAndStatus(BookingType bookingType, BookingStatus status);

    List<Booking> findConflictingBookings(Long courtId, LocalDateTime startTime, LocalDateTime endTime);

    List<Long> findParticipantUserIdsByBookingId(Long bookingId);

    List<Booking> findByParticipantUserId(Long userId);

    List<Booking> findExpiredPendingCasual(LocalDateTime now);

    List<Booking> findExpiredPendingRanked(LocalDateTime timeThreshold);

    List<Booking> findExpiredRankedNoShows(LocalDateTime timeThreshold);

    List<Long> findRecentOpponentUserIds(Long userId, int lastNMatches);

    List<Booking> findActiveRankedMatchesByUserId(Long userId);
}
