package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Court;
import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.VenueRepository;

/**
 * Use Case: Update Court
 * Only venue owner can update their court
 */
public class UpdateCourtUseCase {
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;

    public UpdateCourtUseCase(CourtRepository courtRepository, VenueRepository venueRepository) {
        this.courtRepository = courtRepository;
        this.venueRepository = venueRepository;
    }

    public Court execute(Long courtId, Long requesterId, String courtName) {
        // Find court
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Court không tồn tại"));

        // Find venue to verify ownership
        Venue venue = venueRepository.findById(court.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        // Verify ownership
        if (!venue.getOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("Chỉ chủ sân mới có quyền cập nhật court");
        }

        // Update fields
        court.setCourtName(courtName);

        return courtRepository.save(court);
    }
}
