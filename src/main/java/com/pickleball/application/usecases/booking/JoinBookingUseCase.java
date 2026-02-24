package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class JoinBookingUseCase {
    private final BookingRepository bookingRepository;

    public JoinBookingUseCase(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking execute(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        // Kiểm tra booking status
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Booking is not available to join");
        }

        // Kiểm tra user đã tham gia chưa
        boolean alreadyJoined = booking.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));
        if (alreadyJoined) {
            throw new IllegalArgumentException("User already joined this booking");
        }

        // Thêm user vào booking
        BookingParticipant participant = BookingParticipant.builder()
                .bookingId(booking.getId())
                .userId(userId)
                .role(ParticipantRole.PLAYER)
                .joinStatus(JoinStatus.PAID)
                .depositAmount(new Money(new BigDecimal("50000"), "VND")) // 50k deposit
                .refundAmount(new Money(BigDecimal.ZERO, "VND"))
                .isMatchHost(false)
                .build();

        booking.addParticipant(participant);

        // Kiểm tra xem đã đủ người chưa (ví dụ: cần 4 người cho casual match)
        long playerCount = booking.getParticipants().stream()
                .filter(p -> p.getRole() == ParticipantRole.PLAYER)
                .count();

        if (playerCount >= 4 && booking.getBookingType() == com.pickleball.domain.enums.BookingType.CASUAL) {
            booking.confirm();
        }

        return bookingRepository.save(booking);
    }
}