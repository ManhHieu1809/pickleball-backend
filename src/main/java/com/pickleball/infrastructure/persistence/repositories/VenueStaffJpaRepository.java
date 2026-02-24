package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.VenueStaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueStaffJpaRepository extends JpaRepository<VenueStaffEntity, Long> {
    Optional<VenueStaffEntity> findByUsername(String username);
    List<VenueStaffEntity> findByVenueId(Long venueId);
    List<VenueStaffEntity> findByVenueIdAndIsActiveTrue(Long venueId);
    boolean existsByUsername(String username);
}
