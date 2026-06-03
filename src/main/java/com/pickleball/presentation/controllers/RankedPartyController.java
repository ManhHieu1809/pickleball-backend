package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.RankedPartyDTO;
import com.pickleball.application.dtos.PendingInviteCountDTO;
import com.pickleball.application.dtos.RankedAvailabilityDTO;
import com.pickleball.application.dtos.requests.CheckRankedAvailabilityRequest;
import com.pickleball.application.dtos.requests.CreateRankedPartyRequest;
import com.pickleball.application.dtos.requests.InviteRankedPartyMemberRequest;
import com.pickleball.application.dtos.requests.QueueRankedPartyRequest;
import com.pickleball.application.services.RankedPartyApplicationService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranked-parties")
@RequiredArgsConstructor
public class RankedPartyController {

    private final RankedPartyApplicationService rankedPartyService;

    @PostMapping
    public ResponseEntity<ApiResponse<RankedPartyDTO>> createParty(
            @Valid @RequestBody CreateRankedPartyRequest request) {
        RankedPartyDTO party = rankedPartyService.createParty(request);
        return ResponseHelper.created(party, "Ranked party created");
    }

    @GetMapping("/{partyId}")
    public ResponseEntity<ApiResponse<RankedPartyDTO>> getParty(@PathVariable Long partyId) {
        RankedPartyDTO party = rankedPartyService.getParty(partyId);
        return ResponseHelper.ok(party, "Ranked party retrieved");
    }

    @GetMapping("/invites/my")
    public ResponseEntity<ApiResponse<List<RankedPartyDTO>>> getMyInvites(@RequestParam Long userId) {
        List<RankedPartyDTO> invites = rankedPartyService.getMyInvites(userId);
        return ResponseHelper.ok(invites, "Ranked party invites retrieved");
    }

    @GetMapping("/invites/pending-count")
    public ResponseEntity<ApiResponse<PendingInviteCountDTO>> getPendingInviteCount(@RequestParam Long userId) {
        PendingInviteCountDTO count = rankedPartyService.getPendingInviteCount(userId);
        return ResponseHelper.ok(count, "Pending ranked party invite count retrieved");
    }

    @GetMapping("/my-active")
    public ResponseEntity<ApiResponse<RankedPartyDTO>> getMyActiveParty(@RequestParam Long userId) {
        RankedPartyDTO party = rankedPartyService.getMyActiveParty(userId);
        return ResponseHelper.ok(party, party == null ? "No active ranked party" : "Active ranked party retrieved");
    }

    @GetMapping("/my-active-list")
    public ResponseEntity<ApiResponse<List<RankedPartyDTO>>> getMyActiveParties(@RequestParam Long userId) {
        List<RankedPartyDTO> parties = rankedPartyService.getMyActiveParties(userId);
        return ResponseHelper.ok(parties, "Active ranked parties retrieved");
    }

    @PostMapping("/check-availability")
    public ResponseEntity<ApiResponse<RankedAvailabilityDTO>> checkAvailability(
            @Valid @RequestBody CheckRankedAvailabilityRequest request) {
        RankedAvailabilityDTO availability = rankedPartyService.checkAvailability(request);
        return ResponseHelper.ok(availability, "Ranked court availability checked");
    }

    @PostMapping("/{partyId}/invites")
    public ResponseEntity<ApiResponse<RankedPartyDTO>> inviteMember(
            @PathVariable Long partyId,
            @Valid @RequestBody InviteRankedPartyMemberRequest request) {
        RankedPartyDTO party = rankedPartyService.inviteMember(partyId, request);
        return ResponseHelper.ok(party, "Ranked party invite sent");
    }

    @PostMapping("/{partyId}/invites/accept")
    public ResponseEntity<ApiResponse<RankedPartyDTO>> acceptInvite(
            @PathVariable Long partyId,
            @RequestParam Long userId) {
        RankedPartyDTO party = rankedPartyService.acceptInvite(partyId, userId);
        return ResponseHelper.ok(party, "Ranked party invite accepted");
    }

    @PostMapping("/{partyId}/invites/reject")
    public ResponseEntity<ApiResponse<RankedPartyDTO>> rejectInvite(
            @PathVariable Long partyId,
            @RequestParam Long userId) {
        RankedPartyDTO party = rankedPartyService.rejectInvite(partyId, userId);
        return ResponseHelper.ok(party, "Ranked party invite rejected");
    }

    @PostMapping("/{partyId}/leave")
    public ResponseEntity<ApiResponse<RankedPartyDTO>> leaveParty(
            @PathVariable Long partyId,
            @RequestParam Long userId) {
        RankedPartyDTO party = rankedPartyService.leaveParty(partyId, userId);
        return ResponseHelper.ok(party, "Left ranked party");
    }

    @PostMapping("/{partyId}/members/{memberUserId}/remove")
    public ResponseEntity<ApiResponse<RankedPartyDTO>> removeMember(
            @PathVariable Long partyId,
            @PathVariable Long memberUserId,
            @RequestParam Long hostUserId) {
        RankedPartyDTO party = rankedPartyService.removeMember(partyId, hostUserId, memberUserId);
        return ResponseHelper.ok(party, "Ranked party member removed");
    }

    @PostMapping("/{partyId}/queue")
    public ResponseEntity<ApiResponse<RankedPartyDTO>> queueParty(
            @PathVariable Long partyId,
            @Valid @RequestBody QueueRankedPartyRequest request) {
        RankedPartyDTO party = rankedPartyService.queueParty(partyId, request);
        return ResponseHelper.ok(party, "Ranked party joined matchmaking queue");
    }

    @PostMapping("/{partyId}/cancel-queue")
    public ResponseEntity<ApiResponse<RankedPartyDTO>> cancelQueue(
            @PathVariable Long partyId,
            @RequestParam Long userId) {
        RankedPartyDTO party = rankedPartyService.cancelQueue(partyId, userId);
        return ResponseHelper.ok(party, "Ranked party matchmaking cancelled");
    }
}
