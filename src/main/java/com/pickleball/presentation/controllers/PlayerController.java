package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.requests.UpdateLocationRequest;
import com.pickleball.application.dtos.EloHistoryDTO;
import com.pickleball.application.usecases.player.UpdatePlayerLocationUseCase;
import com.pickleball.application.usecases.player.GetEloHistoryUseCase;
import com.pickleball.application.usecases.player.GetPlayerProfileUseCase;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final UpdatePlayerLocationUseCase updatePlayerLocationUseCase;
    private final GetEloHistoryUseCase getEloHistoryUseCase;
    private final GetPlayerProfileUseCase getPlayerProfileUseCase;
    private final com.pickleball.application.usecases.player.GetPlayerWeeklyStatsUseCase getPlayerWeeklyStatsUseCase;
    private final com.pickleball.application.usecases.player.GetPlayerRankedStatsUseCase getPlayerRankedStatsUseCase;

    @PutMapping("/location")
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @Valid @RequestBody UpdateLocationRequest request) {
        updatePlayerLocationUseCase.execute(request);
        return ResponseHelper.ok("Location updated successfully");
    }

    @GetMapping("/{userId}/elo-history")
    public ResponseEntity<ApiResponse<List<EloHistoryDTO>>> getEloHistory(@PathVariable Long userId) {
        List<EloHistoryDTO> history = getEloHistoryUseCase.execute(userId);
        return ResponseHelper.ok(history);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<com.pickleball.application.dtos.PlayerMatchDTO>> getPlayerProfile(@PathVariable Long userId) {
        return ResponseHelper.ok(getPlayerProfileUseCase.execute(userId));
    }

    @GetMapping("/{userId}/stats/weekly")
    public ResponseEntity<ApiResponse<com.pickleball.application.dtos.PlayerWeeklyStatsDTO>> getPlayerWeeklyStats(@PathVariable Long userId) {
        return ResponseHelper.ok(getPlayerWeeklyStatsUseCase.execute(userId));
    }

    @GetMapping("/{userId}/stats/ranked")
    public ResponseEntity<ApiResponse<com.pickleball.application.dtos.PlayerRankedStatsDTO>> getPlayerRankedStats(@PathVariable Long userId) {
        return ResponseHelper.ok(getPlayerRankedStatsUseCase.execute(userId));
    }
}
