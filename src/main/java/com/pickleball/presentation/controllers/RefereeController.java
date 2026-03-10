package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.*;
import com.pickleball.application.dtos.requests.*;
import com.pickleball.application.services.RefereeApplicationService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/referee")
@RequiredArgsConstructor
public class RefereeController {

    private final RefereeApplicationService refereeApplicationService;

    // ==================== AI Test Endpoints ====================

    /**
     * Generate 10 random questions (2 per category) for referee test.
     */
    @GetMapping("/test/generate")
    public ResponseEntity<ApiResponse<List<TestQuestionDTO>>> generateTest() {
        List<TestQuestionDTO> questions = refereeApplicationService.generateTest();
        return ResponseHelper.ok(questions);
    }

    /**
     * Submit test answers. If score >= 9/10, auto-creates referee registration request.
     */
    @PostMapping("/test/submit")
    public ResponseEntity<ApiResponse<RefereeTestResultDTO>> submitTest(
            @Valid @RequestBody SubmitTestAnswersRequest request) {
        RefereeTestResultDTO result = refereeApplicationService.submitTest(request);
        return ResponseHelper.ok(result);
    }

    /**
     * Get test attempt history for a player.
     */
    @GetMapping("/test/history")
    public ResponseEntity<ApiResponse<List<RefereeTestResultDTO>>> getTestHistory(
            @RequestParam Long userId) {
        List<RefereeTestResultDTO> history = refereeApplicationService.getTestHistory(userId);
        return ResponseHelper.ok(history);
    }

    // ==================== Referee Profile ====================

    /**
     * Get referee profile (trust score, match count, etc.)
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<RefereeDTO>> getRefereeProfile(
            @RequestParam Long userId) {
        RefereeDTO profile = refereeApplicationService.getRefereeProfile(userId);
        return ResponseHelper.ok(profile);
    }

    // ==================== Match Result (Referee Only) ====================

    /**
     * Referee submits match result (score + winning team).
     */
    @PostMapping("/matches/{matchId}/result")
    public ResponseEntity<ApiResponse<Void>> submitMatchResult(
            @PathVariable Long matchId,
            @Valid @RequestBody SubmitMatchResultRequest request) {
        refereeApplicationService.submitMatchResult(matchId, request);
        return ResponseHelper.ok(null, "Match result submitted successfully");
    }

    // ==================== Dispute - Referee Evidence ====================

    /**
     * Referee submits evidence for a dispute (within 24h deadline).
     */
    @PostMapping("/disputes/{disputeId}/evidence")
    public ResponseEntity<ApiResponse<MatchDisputeDTO>> submitEvidence(
            @PathVariable Long disputeId,
            @Valid @RequestBody SubmitRefereeEvidenceRequest request) {
        MatchDisputeDTO dispute = refereeApplicationService.submitRefereeEvidence(disputeId, request);
        return ResponseHelper.ok(dispute, "Evidence submitted successfully");
    }

    // ==================== Dispute - Player Submit ====================

    /**
     * Player submits a dispute against referee's result.
     */
    @PostMapping("/disputes")
    public ResponseEntity<ApiResponse<MatchDisputeDTO>> submitDispute(
            @Valid @RequestBody SubmitDisputeRequest request) {
        MatchDisputeDTO dispute = refereeApplicationService.submitDispute(request);
        return ResponseHelper.created(dispute);
    }
}
