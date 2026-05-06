package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.VenueRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ToggleVenueStatusUseCase {
    private final VenueRepository venueRepository;

    public Venue execute(Long venueId, Long requesterId, boolean activate, boolean isAdmin) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (!isAdmin && !venue.getOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("Chỉ chủ sân hoặc admin mới có quyền thay đổi trạng thái venue");
        }

        if (activate) {
            if (isAdmin) {
                venue.activateByAdmin();
            } else {
                venue.activate();
            }
        } else {
            venue.deactivate(requesterId, isAdmin);
        }

        return venueRepository.save(venue);
    }
}

