package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import com.pickleball.infrastructure.persistence.mappers.BookingMapper;
import com.pickleball.infrastructure.persistence.repositories.BookingJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BookingRepositoryAdapter implements BookingRepository {

    private final BookingJpaRepository bookingJpaRepository;
    private final BookingMapper bookingMapper;

    public BookingRepositoryAdapter(BookingJpaRepository bookingJpaRepository, BookingMapper bookingMapper) {
        this.bookingJpaRepository = bookingJpaRepository;
        this.bookingMapper = bookingMapper;
    }

    @Override
    public Booking save(Booking booking) {
        BookingEntity entity = bookingMapper.toEntity(booking);
        BookingEntity saved = bookingJpaRepository.save(entity);
        return bookingMapper.toDomain(saved);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return bookingJpaRepository.findById(id)
                .map(bookingMapper::toDomain);
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
        return bookingJpaRepository.findByBookingTypeAndStatus(bookingType, status).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findConflictingBookings(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
        return bookingJpaRepository.findConflictingBookings(courtId, startTime, endTime).stream()
                .map(bookingMapper::toDomain)
                .collect(Collectors.toList());
    }
}