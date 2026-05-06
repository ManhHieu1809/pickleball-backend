package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.VenueRepository;

public class ApproveVenueUseCase {
    private final VenueRepository venueRepository;

    public ApproveVenueUseCase(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue execute(Long venueId, Long adminId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (venue.isActive() && venue.getApprovedByAdminId() != null) {
            throw new IllegalArgumentException("Venue đã được duyệt trước đó");
        }

        venue.approve(adminId);

        return venueRepository.save(venue);
    }
}
