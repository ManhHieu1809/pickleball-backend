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

    @GetMapping("/test/generate")
    public ResponseEntity<ApiResponse<List<TestQuestionDTO>>> generateTest() {
        List<TestQuestionDTO> questions = refereeApplicationService.generateTest();
        return ResponseHelper.ok(questions);
    }

    @PostMapping("/test/submit")
    public ResponseEntity<ApiResponse<RefereeTestResultDTO>> submitTest(
            @Valid @RequestBody SubmitTestAnswersRequest request) {
        RefereeTestResultDTO result = refereeApplicationService.submitTest(request);
        return ResponseHelper.ok(result);
    }

    @GetMapping("/test/history")
    public ResponseEntity<ApiResponse<List<RefereeTestResultDTO>>> getTestHistory(
            @RequestParam Long userId) {
        List<RefereeTestResultDTO> history = refereeApplicationService.getTestHistory(userId);
        return ResponseHelper.ok(history);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<RefereeDTO>> getRefereeProfile(
            @RequestParam Long userId) {
        RefereeDTO profile = refereeApplicationService.getRefereeProfile(userId);
        return ResponseHelper.ok(profile);
    }

    @PutMapping("/{refereeId}/availability")
    public ResponseEntity<ApiResponse<RefereeDTO>> toggleAvailability(
            @PathVariable Long refereeId,
            @RequestParam boolean isReady) {
        RefereeDTO updatedProfile = refereeApplicationService.toggleAvailability(refereeId, isReady);
        return ResponseHelper.ok(updatedProfile, "Availability updated to " + isReady);
    }

    @GetMapping("/{refereeId}/matches")
    public ResponseEntity<ApiResponse<List<RankedMatchDTO>>> getRefereeMatches(
            @PathVariable Long refereeId,
            @RequestParam(required = false) String status) {
        List<RankedMatchDTO> matches = refereeApplicationService.getRefereeMatches(refereeId, status);
        return ResponseHelper.ok(matches);
    }

    @PostMapping("/matches/{matchId}/result")
    public ResponseEntity<ApiResponse<Void>> submitMatchResult(
            @PathVariable Long matchId,
            @Valid @RequestBody SubmitMatchResultRequest request) {
        refereeApplicationService.submitMatchResult(matchId, request);
        return ResponseHelper.ok(null, "Match result submitted successfully");
    }

    @PostMapping("/disputes/{disputeId}/evidence")
    public ResponseEntity<ApiResponse<MatchDisputeDTO>> submitEvidence(
            @PathVariable Long disputeId,
            @Valid @RequestBody SubmitRefereeEvidenceRequest request) {
        MatchDisputeDTO dispute = refereeApplicationService.submitRefereeEvidence(disputeId, request);
        return ResponseHelper.ok(dispute, "Evidence submitted successfully");
    }

    @GetMapping("/{refereeId}/disputes")
    public ResponseEntity<ApiResponse<List<MatchDisputeDTO>>> getRefereeDisputes(
            @PathVariable Long refereeId) {
        List<MatchDisputeDTO> disputes = refereeApplicationService.getRefereeDisputes(refereeId);
        return ResponseHelper.ok(disputes);
    }

    @PostMapping("/disputes")
    public ResponseEntity<ApiResponse<MatchDisputeDTO>> submitDispute(
            @Valid @RequestBody SubmitDisputeRequest request) {
        MatchDisputeDTO dispute = refereeApplicationService.submitDispute(request);
        return ResponseHelper.created(dispute);
    }
}
