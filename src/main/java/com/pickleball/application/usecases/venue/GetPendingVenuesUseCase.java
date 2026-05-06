package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.VenueRepository;

import java.util.List;

public class GetPendingVenuesUseCase {
    private final VenueRepository venueRepository;

    public GetPendingVenuesUseCase(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<Venue> execute() {
        return venueRepository.findPendingVenues();
    }
}
