package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.CheckInEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckInJpaRepository extends JpaRepository<CheckInEntity, Long> {
    Optional<CheckInEntity> findByBookingIdAndUserId(Long bookingId, Long userId);
}
