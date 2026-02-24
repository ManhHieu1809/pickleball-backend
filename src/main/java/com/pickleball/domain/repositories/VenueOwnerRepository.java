package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.VenueOwner;

import java.util.Optional;

public interface VenueOwnerRepository {
    VenueOwner save(VenueOwner venueOwner);
    Optional<VenueOwner> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}

