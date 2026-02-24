package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.VenueRepository;

public class RejectVenueUseCase {
    private final VenueRepository venueRepository;

    public RejectVenueUseCase(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue execute(Long venueId, Long adminId, String reason) {
        // Find venue
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        // Check if already approved
        if (venue.isActive() && venue.getApprovedByAdminId() != null) {
            throw new IllegalArgumentException("Không thể từ chối venue đã được duyệt");
        }

        // Reject venue (deactivate with admin ID)
        venue.deactivate(adminId, true);

        return venueRepository.save(venue);
    }
}
