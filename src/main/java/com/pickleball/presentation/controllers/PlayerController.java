package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.requests.UpdateLocationRequest;
import com.pickleball.domain.entities.EloHistory;
import com.pickleball.domain.repositories.EloHistoryRepository;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerRepository playerRepository;
    private final EloHistoryRepository eloHistoryRepository;

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

    @GetMapping("/{userId}/elo-history")
    public ResponseEntity<ApiResponse<List<EloHistory>>> getEloHistory(@PathVariable Long userId) {
        List<EloHistory> history = eloHistoryRepository.findByUserId(userId);
        return ResponseHelper.ok(history);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<com.pickleball.application.dtos.PlayerMatchDTO>> getPlayerProfile(@PathVariable Long userId) {
        return playerRepository.findByUserId(userId)
                .map(player -> {
                    com.pickleball.application.dtos.PlayerMatchDTO dto = new com.pickleball.application.dtos.PlayerMatchDTO();
                    dto.setUserId(player.getUserId());
                    dto.setCurrentElo(player.getCurrentElo());
                    dto.setLoyaltyTier(player.getLoyaltyTier() != null ? player.getLoyaltyTier().name() : null);
                    // fullName isn't on Player entity but we can fetch it if needed, or leave it mapped purely for Elo matching. 
                    return ResponseHelper.ok(dto);
                })
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }
}
