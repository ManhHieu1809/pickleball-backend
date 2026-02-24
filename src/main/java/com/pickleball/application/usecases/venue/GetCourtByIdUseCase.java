package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Court;
import com.pickleball.domain.repositories.CourtRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetCourtByIdUseCase {
    private final CourtRepository courtRepository;

    public Court execute(Long courtId) {
        return courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Court không tồn tại"));
    }
}

