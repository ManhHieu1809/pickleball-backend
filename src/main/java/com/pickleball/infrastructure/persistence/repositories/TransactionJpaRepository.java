package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByUserId(Long userId);
    List<TransactionEntity> findByBookingId(Long bookingId);
}

