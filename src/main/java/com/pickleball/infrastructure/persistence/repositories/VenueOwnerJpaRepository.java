package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.VenueOwnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VenueOwnerJpaRepository extends JpaRepository<VenueOwnerEntity, Long> {
    Optional<VenueOwnerEntity> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}

