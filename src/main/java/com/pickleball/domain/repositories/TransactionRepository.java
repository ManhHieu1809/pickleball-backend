package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.Transaction;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(Long id);
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByBookingId(Long bookingId);
    Optional<Transaction> findByTransactionCode(String transactionCode);
    List<Transaction> findByTypeAndStatus(String type, String status);
}

