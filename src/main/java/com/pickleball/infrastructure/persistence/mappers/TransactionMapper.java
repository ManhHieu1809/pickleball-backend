package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.Transaction;
import com.pickleball.infrastructure.persistence.entities.TransactionEntity;

public class TransactionMapper {
    public static Transaction toDomain(TransactionEntity entity) {
        if (entity == null) return null;
        return Transaction.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .bookingId(entity.getBookingId())
                .amount(entity.getAmount())
                .type(entity.getType())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static TransactionEntity toEntity(Transaction domain) {
        if (domain == null) return null;
        return TransactionEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .bookingId(domain.getBookingId())
                .amount(domain.getAmount())
                .type(domain.getType())
                .status(domain.getStatus())
                .description(domain.getDescription())
                .build();
    }
}

