package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByUserId(Long userId);
    List<TransactionEntity> findByBookingId(Long bookingId);

    @Query("SELECT t FROM TransactionEntity t " +
            "WHERE (:search IS NULL OR " +
            "  CAST(t.id AS string) LIKE %:search% OR " +
            "  t.description LIKE %:search%) AND " +
            "(:type IS NULL OR t.type = :type) AND " +
            "(:status IS NULL OR t.status = :status)")
    Page<TransactionEntity> searchTransactions(
            @Param("search") String search,
            @Param("type") String type,
            @Param("status") String status,
            Pageable pageable);
}
