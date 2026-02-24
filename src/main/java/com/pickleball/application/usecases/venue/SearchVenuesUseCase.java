package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.VenueRepository;

import java.util.List;

public class SearchVenuesUseCase {
    private final VenueRepository venueRepository;

    public SearchVenuesUseCase(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<Venue> executeActiveVenues() {
        return venueRepository.findActiveVenues();
    }

    public List<Venue> executeNearbyVenues(Double latitude, Double longitude, Double radiusKm) {
        return venueRepository.findNearbyVenues(latitude, longitude, radiusKm);
    }
}