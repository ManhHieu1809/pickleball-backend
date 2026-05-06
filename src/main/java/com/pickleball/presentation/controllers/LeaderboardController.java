package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.LeaderboardPageDTO;
import com.pickleball.application.services.LeaderboardApplicationService;
import com.pickleball.presentation.responses.ApiResponse;
import com.pickleball.presentation.helpers.ResponseHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardApplicationService leaderboardApplicationService;

    public LeaderboardController(LeaderboardApplicationService leaderboardApplicationService) {
        this.leaderboardApplicationService = leaderboardApplicationService;
    }

    @GetMapping("/global")
    public ResponseEntity<ApiResponse<LeaderboardPageDTO>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LeaderboardPageDTO result = leaderboardApplicationService.getGlobalLeaderboard(page, size);
        return ResponseHelper.ok(result);
    }
}


