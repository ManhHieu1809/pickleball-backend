package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.VenueDTO;
import com.pickleball.application.dtos.requests.CreateVenueRequest;
import com.pickleball.application.services.VenueApplicationService;
import com.pickleball.presentation.responses.ApiResponse;
import com.pickleball.presentation.helpers.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueApplicationService venueService;

    @PostMapping
    public ResponseEntity<ApiResponse<VenueDTO>> createVenue(
            @Valid @RequestBody CreateVenueRequest request) {
        VenueDTO venueDTO = venueService.createVenue(request);
        return ResponseHelper.created(venueDTO);
    }

    @PostMapping("/{venueId}/approve")
    public ResponseEntity<ApiResponse<VenueDTO>> approveVenue(
            @PathVariable Long venueId,
            @RequestParam Long adminId) {
        VenueDTO venueDTO = venueService.approveVenue(venueId, adminId);
        return ResponseHelper.ok(venueDTO, "Venue approved successfully");
    }

    @PostMapping("/{venueId}/reject")
    public ResponseEntity<ApiResponse<VenueDTO>> rejectVenue(
            @PathVariable Long venueId,
            @RequestParam Long adminId,
            @RequestParam(required = false) String reason) {
        VenueDTO venueDTO = venueService.rejectVenue(venueId, adminId, reason);
        return ResponseHelper.ok(venueDTO, "Venue rejected");
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<VenueDTO>>> getPendingVenues() {
        List<VenueDTO> venues = venueService.getPendingVenues();
        return ResponseHelper.ok(venues);
    }

    @PutMapping("/{venueId}")
    public ResponseEntity<ApiResponse<VenueDTO>> updateVenue(
            @PathVariable Long venueId,
            @Valid @RequestBody CreateVenueRequest request,
            @RequestParam Long ownerId) {
        VenueDTO venueDTO = venueService.updateVenue(venueId, request, ownerId);
        return ResponseHelper.ok(venueDTO, "Venue updated successfully");
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<VenueDTO>>> getActiveVenues() {
        List<VenueDTO> venues = venueService.getActiveVenues();
        return ResponseHelper.ok(venues);
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<VenueDTO>>> getNearbyVenues(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radiusKm) {
        List<VenueDTO> venues = venueService.getNearbyVenues(latitude, longitude, radiusKm);
        return ResponseHelper.ok(venues);
    }

    @GetMapping("/{venueId}")
    public ResponseEntity<ApiResponse<VenueDTO>> getVenueById(@PathVariable Long venueId) {
        VenueDTO venueDTO = venueService.getVenueById(venueId);
        return ResponseHelper.ok(venueDTO);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<VenueDTO>>> getVenuesByOwner(@PathVariable Long ownerId) {
        List<VenueDTO> venues = venueService.getVenuesByOwner(ownerId);
        return ResponseHelper.ok(venues);
    }

    @PutMapping("/{venueId}/activate")
    public ResponseEntity<ApiResponse<VenueDTO>> activateVenue(
            @PathVariable Long venueId,
            @RequestParam Long requesterId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        VenueDTO venueDTO = venueService.activateVenue(venueId, requesterId, isAdmin);
        return ResponseHelper.ok(venueDTO, "Venue đã được kích hoạt");
    }

    @PutMapping("/{venueId}/deactivate")
    public ResponseEntity<ApiResponse<VenueDTO>> deactivateVenue(
            @PathVariable Long venueId,
            @RequestParam Long requesterId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        VenueDTO venueDTO = venueService.deactivateVenue(venueId, requesterId, isAdmin);
        return ResponseHelper.ok(venueDTO, "Venue đã được vô hiệu hóa");
    }
}