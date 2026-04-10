package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.Transaction;
import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByBookingId(Long bookingId);
}

