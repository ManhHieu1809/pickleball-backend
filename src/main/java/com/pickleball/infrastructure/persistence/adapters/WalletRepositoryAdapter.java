package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.Wallet;
import com.pickleball.domain.repositories.WalletRepository;
import com.pickleball.infrastructure.persistence.entities.WalletEntity;
import com.pickleball.infrastructure.persistence.mappers.WalletMapper;
import com.pickleball.infrastructure.persistence.repositories.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepository {
    private final WalletJpaRepository jpaRepository;

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity entity = WalletMapper.toEntity(wallet);
        return WalletMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Wallet> findByUserId(Long userId) {
        return jpaRepository.findById(userId).map(WalletMapper::toDomain);
    }
}

