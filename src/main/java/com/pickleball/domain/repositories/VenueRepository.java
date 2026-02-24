package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.Venue;
import java.util.List;
import java.util.Optional;

public interface VenueRepository {
    Venue save(Venue venue);
    Optional<Venue> findById(Long id);
    List<Venue> findByOwnerId(Long ownerId);
    List<Venue> findActiveVenues();
    List<Venue> findNearbyVenues(Double latitude, Double longitude, Double radiusKm);
    List<Venue> findPendingVenues();  // Venues waiting for admin approval
}
