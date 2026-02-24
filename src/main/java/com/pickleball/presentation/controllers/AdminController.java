package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.RoleRequestDTO;
import com.pickleball.application.dtos.requests.RejectRequestRequest;
import com.pickleball.application.dtos.requests.SubmitVenueOwnerRequest;
import com.pickleball.application.services.AdminApplicationService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminApplicationService adminService;

    @PostMapping("/role-requests/venue-owner")
    public ResponseEntity<ApiResponse<RoleRequestDTO>> submitVenueOwnerRequest(
            @Valid @RequestBody SubmitVenueOwnerRequest request) {
        RoleRequestDTO roleRequest = adminService.submitVenueOwnerRequest(request);
        return ResponseHelper.created(roleRequest);
    }

    @GetMapping("/role-requests/pending")
    public ResponseEntity<ApiResponse<List<RoleRequestDTO>>> getPendingRequests() {
        List<RoleRequestDTO> requests = adminService.getPendingRequests();
        return ResponseHelper.ok(requests);
    }

    @PostMapping("/role-requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<RoleRequestDTO>> approveRequest(
            @PathVariable Long requestId,
            @RequestParam Long adminId) {
        RoleRequestDTO roleRequest = adminService.approveRequest(requestId, adminId);
        return ResponseHelper.ok(roleRequest, "Request approved successfully");
    }

    @PostMapping("/role-requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<RoleRequestDTO>> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam Long adminId,
            @Valid @RequestBody RejectRequestRequest request) {
        RoleRequestDTO roleRequest = adminService.rejectRequest(requestId, adminId, request.getNotes());
        return ResponseHelper.ok(roleRequest, "Request rejected successfully");
    }
}

