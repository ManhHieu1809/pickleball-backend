package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.domain.enums.RefereeType;
import com.pickleball.infrastructure.persistence.entities.RefereeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefereeJpaRepository extends JpaRepository<RefereeEntity, Long> {
    Optional<RefereeEntity> findByUserId(Long userId);
    List<RefereeEntity> findByRefereeType(RefereeType refereeType);
    List<RefereeEntity> findByWorksAtVenueId(Long venueId);
    List<RefereeEntity> findByIsActiveTrue();
}