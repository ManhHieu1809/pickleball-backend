package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.CourtPricingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourtPricingJpaRepository extends JpaRepository<CourtPricingEntity, Long> {
    List<CourtPricingEntity> findByCourtId(Long courtId);
    List<CourtPricingEntity> findByCourtIdAndDayOfWeek(Long courtId, Integer dayOfWeek);
}