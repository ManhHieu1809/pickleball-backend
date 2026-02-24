package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.repositories.CourtPricingRepository;
import com.pickleball.infrastructure.persistence.entities.CourtPricingEntity;
import com.pickleball.infrastructure.persistence.mappers.CourtPricingMapper;
import com.pickleball.infrastructure.persistence.repositories.CourtPricingJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CourtPricingRepositoryAdapter implements CourtPricingRepository {

    private final CourtPricingJpaRepository courtPricingJpaRepository;
    private final CourtPricingMapper courtPricingMapper;

    public CourtPricingRepositoryAdapter(CourtPricingJpaRepository courtPricingJpaRepository,
                                         CourtPricingMapper courtPricingMapper) {
        this.courtPricingJpaRepository = courtPricingJpaRepository;
        this.courtPricingMapper = courtPricingMapper;
    }

    @Override
    public CourtPricing save(CourtPricing courtPricing) {
        CourtPricingEntity entity = courtPricingMapper.toEntity(courtPricing);
        CourtPricingEntity saved = courtPricingJpaRepository.save(entity);
        return courtPricingMapper.toDomain(saved);
    }

    @Override
    public List<CourtPricing> findByCourtId(Long courtId) {
        return courtPricingJpaRepository.findByCourtId(courtId).stream()
                .map(courtPricingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourtPricing> findByCourtIdAndDayOfWeek(Long courtId, Integer dayOfWeek) {
        return courtPricingJpaRepository.findByCourtIdAndDayOfWeek(courtId, dayOfWeek).stream()
                .map(courtPricingMapper::toDomain)
                .collect(Collectors.toList());
    }
}