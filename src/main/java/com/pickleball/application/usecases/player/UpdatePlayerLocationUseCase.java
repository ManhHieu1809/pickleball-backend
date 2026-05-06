package com.pickleball.application.usecases.player;

import com.pickleball.application.dtos.requests.UpdateLocationRequest;
import com.pickleball.domain.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UpdatePlayerLocationUseCase {

    private final PlayerRepository playerRepository;

    @Transactional
    public void execute(UpdateLocationRequest request) {
        playerRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        playerRepository.updateLocation(request.getUserId(), request.getLatitude(), request.getLongitude());
    }
}
