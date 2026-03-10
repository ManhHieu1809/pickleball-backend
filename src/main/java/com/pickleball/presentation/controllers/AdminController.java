package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.*;
import com.pickleball.application.dtos.requests.RejectRequestRequest;
import com.pickleball.application.dtos.requests.ResolveDisputeRequest;
import com.pickleball.application.dtos.requests.SubmitVenueOwnerRequest;
import com.pickleball.application.services.AdminApplicationService;
import com.pickleball.application.services.BookingManagementService;
import com.pickleball.application.services.DashboardService;
import com.pickleball.application.services.RefereeApplicationService;
import com.pickleball.application.services.UserManagementService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import com.pickleball.presentation.responses.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminApplicationService adminService;
    private final UserManagementService userManagementService;
    private final DashboardService dashboardService;
    private final BookingManagementService bookingManagementService;
    private final com.pickleball.application.services.VenueManagementService venueManagementService;
    private final RefereeApplicationService refereeApplicationService;

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        return ResponseHelper.ok(stats);
    }

    // ==================== Role Request Endpoints ====================

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

    // ==================== User Management Endpoints ====================

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PaginatedResponse<AdminUserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {
        Page<AdminUserDTO> userPage = userManagementService.getAllUsers(page, size, search, role);
        return ResponseHelper.ok(userPage);
    }

    @GetMapping("/users/stats")
    public ResponseEntity<ApiResponse<AdminUserStatsDTO>> getUserStats() {
        AdminUserStatsDTO stats = userManagementService.getUserStats();
        return ResponseHelper.ok(stats);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDTO>> getUserById(@PathVariable Long userId) {
        try {
            AdminUserDTO user = userManagementService.getUserById(userId);
            return ResponseHelper.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseHelper.notFound(e.getMessage());
        }
    }

    // ==================== Booking Management Endpoints ====================

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<PaginatedResponse<AdminBookingDTO>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        Page<AdminBookingDTO> bookingPage = bookingManagementService.getAllBookings(page, size, search, status, type);
        return ResponseHelper.ok(bookingPage);
    }

    @GetMapping("/bookings/stats")
    public ResponseEntity<ApiResponse<com.pickleball.application.dtos.AdminBookingStatsDTO>> getBookingStats() {
        com.pickleball.application.dtos.AdminBookingStatsDTO stats = bookingManagementService.getBookingStats();
        return ResponseHelper.ok(stats);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<ApiResponse<AdminBookingDTO>> getBookingById(@PathVariable Long bookingId) {
        try {
            AdminBookingDTO booking = bookingManagementService.getBookingById(bookingId);
            return ResponseHelper.ok(booking);
        } catch (IllegalArgumentException e) {
            return ResponseHelper.notFound(e.getMessage());
        }
    }

    @PutMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<AdminBookingDTO>> cancelBooking(@PathVariable Long bookingId) {
        try {
            AdminBookingDTO booking = bookingManagementService.cancelBooking(bookingId);
            return ResponseHelper.ok(booking, "Booking cancelled successfully");
        } catch (IllegalArgumentException e) {
            return ResponseHelper.badRequest(e.getMessage());
        }
    }

    // ==================== Venue Management Endpoints ====================

    @GetMapping("/venues")
    public ResponseEntity<ApiResponse<PaginatedResponse<com.pickleball.application.dtos.AdminVenueDTO>>> getAllVenues(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        Page<com.pickleball.application.dtos.AdminVenueDTO> venuePage = venueManagementService.getAllVenues(page, size,
                search, status);
        return ResponseHelper.ok(venuePage);
    }

    @GetMapping("/venues/stats")
    public ResponseEntity<ApiResponse<com.pickleball.application.dtos.AdminVenueStatsDTO>> getVenueStats() {
        com.pickleball.application.dtos.AdminVenueStatsDTO stats = venueManagementService.getVenueStats();
        return ResponseHelper.ok(stats);
    }

    @GetMapping("/venues/{venueId}")
    public ResponseEntity<ApiResponse<com.pickleball.application.dtos.AdminVenueDTO>> getVenueById(
            @PathVariable Long venueId) {
        try {
            com.pickleball.application.dtos.AdminVenueDTO venue = venueManagementService.getVenueById(venueId);
            return ResponseHelper.ok(venue);
        } catch (IllegalArgumentException e) {
            return ResponseHelper.notFound(e.getMessage());
        }
    }

    // ==================== Referee Approval Endpoints ====================

    @PostMapping("/referee-requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<RoleRequestDTO>> approveRefereeRequest(
            @PathVariable Long requestId,
            @RequestParam Long adminId) {
        RoleRequestDTO result = refereeApplicationService.approveRefereeRequest(requestId, adminId);
        return ResponseHelper.ok(result, "Referee request approved successfully");
    }

    @PostMapping("/referee-requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<RoleRequestDTO>> rejectRefereeRequest(
            @PathVariable Long requestId,
            @RequestParam Long adminId,
            @Valid @RequestBody RejectRequestRequest request) {
        RoleRequestDTO result = refereeApplicationService.rejectRefereeRequest(requestId, adminId, request.getNotes());
        return ResponseHelper.ok(result, "Referee request rejected");
    }

    // ==================== Dispute Management Endpoints ====================

    @GetMapping("/disputes")
    public ResponseEntity<ApiResponse<List<MatchDisputeDTO>>> getAllDisputes() {
        List<MatchDisputeDTO> disputes = refereeApplicationService.getAllDisputes();
        return ResponseHelper.ok(disputes);
    }

    @GetMapping("/disputes/{disputeId}")
    public ResponseEntity<ApiResponse<MatchDisputeDTO>> getDisputeById(@PathVariable Long disputeId) {
        try {
            MatchDisputeDTO dispute = refereeApplicationService.getDisputeById(disputeId);
            return ResponseHelper.ok(dispute);
        } catch (IllegalArgumentException e) {
            return ResponseHelper.notFound(e.getMessage());
        }
    }

    @PostMapping("/disputes/{disputeId}/resolve")
    public ResponseEntity<ApiResponse<MatchDisputeDTO>> resolveDispute(
            @PathVariable Long disputeId,
            @Valid @RequestBody ResolveDisputeRequest request) {
        MatchDisputeDTO dispute = refereeApplicationService.resolveDispute(disputeId, request);
        return ResponseHelper.ok(dispute, "Dispute resolved successfully");
    }
}
