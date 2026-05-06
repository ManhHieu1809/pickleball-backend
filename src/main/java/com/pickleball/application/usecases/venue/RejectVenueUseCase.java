package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.VenueRepository;

public class RejectVenueUseCase {
    private final VenueRepository venueRepository;

    public RejectVenueUseCase(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue execute(Long venueId, Long adminId, String reason) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (venue.isActive() && venue.getApprovedByAdminId() != null) {
            throw new IllegalArgumentException("Không thể từ chối venue đã được duyệt");
        }

        venue.deactivate(adminId, true);

        return venueRepository.save(venue);
    }
}
