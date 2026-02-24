package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.valueobjects.Location;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class CreateVenueUseCase {
    private final VenueRepository venueRepository;

    public CreateVenueUseCase(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue execute(Long ownerId, String name, String address,
                         BigDecimal latitude, BigDecimal longitude) {
        Location location = new Location(latitude, longitude);
        Venue venue = Venue.builder()
                .ownerId(ownerId)
                .name(name)
                .address(address)
                .location(location)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .amenities(new ArrayList<>())
                .build();

        return venueRepository.save(venue);
    }
}