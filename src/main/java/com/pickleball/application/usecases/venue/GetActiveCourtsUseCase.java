package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Court;
import com.pickleball.domain.repositories.CourtRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetActiveCourtsUseCase {
    private final CourtRepository courtRepository;

    public List<Court> execute(Long venueId) {
        return courtRepository.findByVenueId(venueId).stream()
                .filter(Court::isActive)
                .collect(Collectors.toList());
    }

    public List<Court> executeAll() {
        return courtRepository.findAll().stream()
                .filter(Court::isActive)
                .collect(Collectors.toList());
    }
}

