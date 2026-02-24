package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.infrastructure.persistence.entities.RankedMatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankedMatchJpaRepository extends JpaRepository<RankedMatchEntity, Long> {
    Optional<RankedMatchEntity> findByBookingId(Long bookingId);
    List<RankedMatchEntity> findByStatus(MatchStatus status);
    List<RankedMatchEntity> findByRefereeId(Long refereeId);
    List<RankedMatchEntity> findBySeasonId(Long seasonId);
}