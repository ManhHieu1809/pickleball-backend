// File: application/usecases/booking/CreateBookingUseCase.java
package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CreateBookingUseCase {
    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;

    public CreateBookingUseCase(BookingRepository bookingRepository,
                                CourtRepository courtRepository) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
    }

    public Booking execute(Long courtId, LocalDateTime startTime, LocalDateTime endTime,
                           BookingType bookingType, Long creatorUserId, boolean isPlayer) {
        // Kiểm tra court có tồn tại không
        courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Court not found"));

        // Kiểm tra xem có booking nào trùng giờ không
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(courtId, startTime, endTime);
        if (!conflictingBookings.isEmpty()) {
            throw new IllegalArgumentException("Time slot is already booked");
        }

        // Tạo booking
        Booking booking = Booking.builder()
                .courtId(courtId)
                .startTime(startTime)
                .endTime(endTime)
                .bookingType(bookingType)
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .createdByPlayerId(isPlayer ? creatorUserId : null)
                .createdByStaffId(isPlayer ? null : creatorUserId)
                .build();

        // Lưu booking để có ID
        booking = bookingRepository.save(booking);

        // Nếu là player tạo, thêm họ vào danh sách participant với vai trò HOST
        if (isPlayer) {
            BookingParticipant host = BookingParticipant.builder()
                    .bookingId(booking.getId())
                    .userId(creatorUserId)
                    .role(ParticipantRole.HOST)
                    .joinStatus(JoinStatus.PAID)
                    .isMatchHost(true)
                    .depositAmount(new Money(BigDecimal.ZERO, "VND"))
                    .refundAmount(new Money(BigDecimal.ZERO, "VND"))
                    .build();
            booking.addParticipant(host);
        }

        // Tính toán chi phí cơ bản (tạm thời fix giá)
        // TODO: Tính giá theo CourtPricing
        Money baseFee = new Money(new BigDecimal("200000"), "VND");
        booking.calculateCosts(baseFee, null, new BigDecimal("0.20")); // 20% platform fee

        return bookingRepository.save(booking);
    }
}