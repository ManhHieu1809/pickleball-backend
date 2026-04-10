package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.CheckIn;

import java.util.Optional;

public interface CheckInRepository {
    CheckIn save(CheckIn checkIn);
    Optional<CheckIn> findByBookingIdAndUserId(Long bookingId, Long userId);
}
