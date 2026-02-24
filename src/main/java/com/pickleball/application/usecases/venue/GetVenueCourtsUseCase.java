package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Court;
import com.pickleball.domain.repositories.CourtRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetVenueCourtsUseCase {
    private final CourtRepository courtRepository;

    public List<Court> execute(Long venueId) {
        return courtRepository.findByVenueId(venueId);
    }
}

