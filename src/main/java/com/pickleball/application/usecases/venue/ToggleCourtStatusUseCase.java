package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Court;
import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.VenueRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ToggleCourtStatusUseCase {
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;

    public Court execute(Long courtId, Long requesterId, boolean activate, boolean isAdmin) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Court không tồn tại"));

        Venue venue = venueRepository.findById(court.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (!isAdmin && !venue.getOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("Chỉ chủ sân hoặc admin mới có quyền thay đổi trạng thái court");
        }

        if (activate) {
            if (isAdmin) {
                court.activateByAdmin();
            } else {
                court.activate();
            }
        } else {
            court.deactivate(requesterId, isAdmin);
        }

        return courtRepository.save(court);
    }
}

