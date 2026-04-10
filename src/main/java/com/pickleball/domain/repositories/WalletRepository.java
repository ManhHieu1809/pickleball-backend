package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.Wallet;
import java.util.Optional;

public interface WalletRepository {
    Wallet save(Wallet wallet);
    Optional<Wallet> findByUserId(Long userId);
}

