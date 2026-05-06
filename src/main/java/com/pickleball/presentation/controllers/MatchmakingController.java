package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.MatchmakingTicketDTO;
import com.pickleball.application.dtos.requests.JoinMatchmakingQueueRequest;
import com.pickleball.application.services.MatchmakingQueueApplicationService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matchmaking")
@RequiredArgsConstructor
public class MatchmakingController {

    private final MatchmakingQueueApplicationService matchmakingQueueService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<MatchmakingTicketDTO>> joinQueue(
            @Valid @RequestBody JoinMatchmakingQueueRequest request) {
        MatchmakingTicketDTO ticket = matchmakingQueueService.joinQueue(request);
        return ResponseHelper.created(ticket, "Successfully joined the matchmaking queue");
    }

    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<Void>> leaveQueue(
            @RequestParam Long userId) {
        matchmakingQueueService.leaveQueue(userId);
        return ResponseHelper.ok(null, "Successfully left the matchmaking queue");
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<MatchmakingTicketDTO>> getQueueStatus(
            @RequestParam Long userId) {
        MatchmakingTicketDTO status = matchmakingQueueService.getMyStatus(userId);
        if (status == null) {
            return ResponseHelper.ok(null, "Not in queue or match found");
        }
        return ResponseHelper.ok(status, "In queue");
    }
}

