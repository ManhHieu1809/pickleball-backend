package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.Wallet;
import com.pickleball.infrastructure.persistence.entities.WalletEntity;

public class WalletMapper {
    public static Wallet toDomain(WalletEntity entity) {
        if (entity == null) return null;
        return Wallet.builder()
                .userId(entity.getUserId())
                .balance(entity.getBalance())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static WalletEntity toEntity(Wallet domain) {
        if (domain == null) return null;
        return WalletEntity.builder()
                .userId(domain.getUserId())
                .balance(domain.getBalance())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}

