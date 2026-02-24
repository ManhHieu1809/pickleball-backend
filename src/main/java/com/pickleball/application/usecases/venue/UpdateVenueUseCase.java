package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.valueobjects.Location;

import java.math.BigDecimal;

/**
 * Use Case: Update Venue
 * Only venue owner can update their venue
 */
public class UpdateVenueUseCase {
    private final VenueRepository venueRepository;

    public UpdateVenueUseCase(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue execute(Long venueId, Long requesterId, String name, String address,
                         BigDecimal latitude, BigDecimal longitude, String description) {
        // Find venue
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        // Verify ownership
        if (!venue.getOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("Chỉ chủ sân mới có quyền cập nhật venue");
        }

        // Update fields
        venue.setName(name);
        venue.setAddress(address);
        venue.setLocation(new Location(latitude, longitude));
        venue.setDescription(description);

        return venueRepository.save(venue);
    }
}
