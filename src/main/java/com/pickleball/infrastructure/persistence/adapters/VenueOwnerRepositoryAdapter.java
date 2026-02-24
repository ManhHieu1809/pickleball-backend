package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.VenueOwner;
import com.pickleball.domain.repositories.VenueOwnerRepository;
import com.pickleball.infrastructure.persistence.entities.VenueOwnerEntity;
import com.pickleball.infrastructure.persistence.mappers.VenueOwnerMapper;
import com.pickleball.infrastructure.persistence.repositories.VenueOwnerJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class VenueOwnerRepositoryAdapter implements VenueOwnerRepository {

    private final VenueOwnerJpaRepository jpaRepository;
    private final VenueOwnerMapper mapper;

    public VenueOwnerRepositoryAdapter(VenueOwnerJpaRepository jpaRepository, VenueOwnerMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public VenueOwner save(VenueOwner venueOwner) {
        VenueOwnerEntity entity = mapper.toEntity(venueOwner);
        VenueOwnerEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<VenueOwner> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaRepository.existsByUserId(userId);
    }
}

