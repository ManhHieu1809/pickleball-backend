package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.VenueRepository;

/**
 * Use Case: Approve Venue (Admin only)
 * Admin approves a pending venue to make it active
 */
public class ApproveVenueUseCase {
    private final VenueRepository venueRepository;

    public ApproveVenueUseCase(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue execute(Long venueId, Long adminId) {
        // Find venue
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        // Check if already approved
        if (venue.isActive() && venue.getApprovedByAdminId() != null) {
            throw new IllegalArgumentException("Venue đã được duyệt trước đó");
        }

        // Approve venue
        venue.approve(adminId);

        return venueRepository.save(venue);
    }
}
