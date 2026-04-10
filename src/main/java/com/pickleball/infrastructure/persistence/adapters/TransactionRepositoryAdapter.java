package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.Transaction;
import com.pickleball.domain.repositories.TransactionRepository;
import com.pickleball.infrastructure.persistence.entities.TransactionEntity;
import com.pickleball.infrastructure.persistence.mappers.TransactionMapper;
import com.pickleball.infrastructure.persistence.repositories.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final TransactionJpaRepository jpaRepository;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = TransactionMapper.toEntity(transaction);
        return TransactionMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Transaction> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(TransactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByBookingId(Long bookingId) {
        return jpaRepository.findByBookingId(bookingId).stream()
                .map(TransactionMapper::toDomain)
                .collect(Collectors.toList());
    }
}

