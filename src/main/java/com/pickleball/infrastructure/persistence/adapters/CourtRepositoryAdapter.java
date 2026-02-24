package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.Court;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.infrastructure.persistence.entities.CourtEntity;
import com.pickleball.infrastructure.persistence.mappers.CourtMapper;
import com.pickleball.infrastructure.persistence.repositories.CourtJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CourtRepositoryAdapter implements CourtRepository {

    private final CourtJpaRepository courtJpaRepository;
    private final CourtMapper courtMapper;

    public CourtRepositoryAdapter(CourtJpaRepository courtJpaRepository, CourtMapper courtMapper) {
        this.courtJpaRepository = courtJpaRepository;
        this.courtMapper = courtMapper;
    }

    @Override
    public Court save(Court court) {
        CourtEntity entity = courtMapper.toEntity(court);
        CourtEntity saved = courtJpaRepository.save(entity);
        return courtMapper.toDomain(saved);
    }

    @Override
    public Optional<Court> findById(Long id) {
        return courtJpaRepository.findById(id)
                .map(courtMapper::toDomain);
    }

    @Override
    public List<Court> findByVenueId(Long venueId) {
        return courtJpaRepository.findByVenueId(venueId).stream()
                .map(courtMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Court> findByVenueIdAndActive(Long venueId) {
        return courtJpaRepository.findByVenueIdAndIsActiveTrue(venueId).stream()
                .map(courtMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Court> findByIsActive(boolean isActive) {
        return courtJpaRepository.findByIsActive(isActive).stream()
                .map(courtMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Court> findAll() {
        return courtJpaRepository.findAll().stream()
                .map(courtMapper::toDomain)
                .collect(Collectors.toList());
    }
}