package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import com.pickleball.infrastructure.persistence.mappers.BookingMapper;
import com.pickleball.infrastructure.persistence.repositories.BookingJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.BookingParticipantJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BookingRepositoryAdapter implements BookingRepository {

    private final BookingJpaRepository bookingJpaRepository;
    private final BookingParticipantJpaRepository bookingParticipantJpaRepository;
    private final BookingMapper bookingMapper;

    public BookingRepositoryAdapter(BookingJpaRepository bookingJpaRepository,
                                    BookingParticipantJpaRepository bookingParticipantJpaRepository,
                                    BookingMapper bookingMapper) {
        this.bookingJpaRepository = bookingJpaRepository;
        this.bookingParticipantJpaRepository = bookingParticipantJpaRepository;
        this.bookingMapper = bookingMapper;
    }

    @Override
    public Booking save(Booking booking) {
        BookingEntity entity = bookingMapper.toEntity(booking);
        BookingEntity saved = bookingJpaRepository.save(entity);

        return bookingJpaRepository.findByIdWithParticipants(saved.getId())
                .map(bookingMapper::toDomain)
                .orElseGet(() -> bookingMapper.toDomain(saved));
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return bookingJpaRepository.findByIdWithParticipants(id)
                .map(bookingMapper::toDomain);
    }

    @Override
    public List<Booking> findByPlayerId(Long playerId) {
        return bookingJpaRepository.findByCreatedByPlayerIdOrderByStartTimeDesc(playerId).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByOwnerId(Long ownerId) {
        return bookingJpaRepository.findByOwnerId(ownerId).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByVenueId(Long venueId) {
        return bookingJpaRepository.findByVenueId(venueId).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByStaffId(Long staffId) {
        return bookingJpaRepository.findByCreatedByStaffIdOrderByStartTimeDesc(staffId).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByCourtIdAndStartTimeBetween(Long courtId, LocalDateTime start, LocalDateTime end) {
        return bookingJpaRepository.findByCourtIdAndStartTimeBetween(courtId, start, end).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByCourtIdAndDate(Long courtId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return bookingJpaRepository.findByCourtIdAndStartTimeBetween(courtId, startOfDay, endOfDay).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByStatus(BookingStatus status) {
        return bookingJpaRepository.findByStatus(status).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByBookingTypeAndStatus(BookingType bookingType, BookingStatus status) {
        return bookingJpaRepository.findByBookingTypeAndStatusWithParticipants(bookingType, status).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findConflictingBookings(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
        return bookingJpaRepository.findConflictingBookings(courtId, startTime, endTime).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findParticipantUserIdsByBookingId(Long bookingId) {
        return bookingParticipantJpaRepository.findByBookingId(bookingId).stream()
                .map(p -> p.getUserId())
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findExpiredPendingCasual(LocalDateTime now) {
        return bookingJpaRepository.findExpiredPendingCasual(now).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findExpiredRankedNoShows(LocalDateTime timeThreshold) {
        return bookingJpaRepository.findExpiredRankedNoShows(timeThreshold).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findRecentOpponentUserIds(Long userId, int lastNMatches) {
        return bookingParticipantJpaRepository.findRecentOpponentUserIds(userId, lastNMatches);
    }
}