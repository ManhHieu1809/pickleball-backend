package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.requests.UpdateLocationRequest;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.presentation.responses.ApiResponse;
import com.pickleball.presentation.helpers.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerRepository playerRepository;

    /**
     * Update player GPS location (called from Android app)
     * PUT /api/players/location
     */
    @PutMapping("/location")
    @Transactional
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @Valid @RequestBody UpdateLocationRequest request) {
        playerRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        playerRepository.updateLocation(
                request.getUserId(),
                request.getLatitude(),
                request.getLongitude());

        return ResponseHelper.ok("Location updated successfully");
    }
}
