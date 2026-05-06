package com.pickleball.infrastructure.persistence.adapters;


import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.infrastructure.persistence.entities.VenueEntity;
import com.pickleball.infrastructure.persistence.mappers.VenueMapper;
import com.pickleball.infrastructure.persistence.repositories.VenueJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class VenueRepositoryAdapter implements VenueRepository {

    private final VenueJpaRepository venueJpaRepository;
    private final VenueMapper venueMapper;

    public VenueRepositoryAdapter(VenueJpaRepository venueJpaRepository, VenueMapper venueMapper) {
        this.venueJpaRepository = venueJpaRepository;
        this.venueMapper = venueMapper;
    }

    @Override
    public Venue save(Venue venue) {
        VenueEntity entity = venueMapper.toEntity(venue);
        VenueEntity saved = venueJpaRepository.save(entity);
        return venueMapper.toDomain(saved);
    }

    @Override
    public Optional<Venue> findById(Long id) {
        return venueJpaRepository.findById(id)
                .map(venueMapper::toDomain);
    }

    @Override
    public List<Venue> findByOwnerId(Long ownerId) {
        return venueJpaRepository.findByOwnerId(ownerId).stream()
                .map(venueMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venue> findActiveVenues() {
        return venueJpaRepository.findByIsActiveTrue().stream()
                .map(venueMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venue> findNearbyVenues(Double latitude, Double longitude, Double radiusKm) {
        return venueJpaRepository.findNearbyVenues(latitude, longitude, radiusKm).stream()
                .map(venueMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Venue> findPendingVenues() {
        return venueJpaRepository.findByIsActiveFalseAndApprovedByAdminIdIsNull().stream()
                .map(venueMapper::toDomain)
                .collect(Collectors.toList());
    }
}