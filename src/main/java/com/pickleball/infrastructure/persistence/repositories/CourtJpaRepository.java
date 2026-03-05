package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.CourtEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtJpaRepository extends JpaRepository<CourtEntity, Long> {
    List<CourtEntity> findByVenueId(Long venueId);

    List<CourtEntity> findByVenueIdAndIsActiveTrue(Long venueId);

    List<CourtEntity> findByIsActive(boolean isActive);

    Long countByVenueId(Long venueId);
}