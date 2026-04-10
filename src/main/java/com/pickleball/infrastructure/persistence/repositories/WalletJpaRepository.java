package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, Long> {
}

