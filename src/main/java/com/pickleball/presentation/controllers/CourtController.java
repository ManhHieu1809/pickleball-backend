package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.CourtDTO;
import com.pickleball.application.dtos.DetailedCourtDTO;
import com.pickleball.application.dtos.requests.CreateCourtRequest;
import com.pickleball.application.services.VenueApplicationService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {
    private final VenueApplicationService venueService;

    @PostMapping
    public ResponseEntity<ApiResponse<CourtDTO>> createCourt(
            @Valid @RequestBody CreateCourtRequest request,
            @RequestParam Long ownerId) {
        CourtDTO courtDTO = venueService.createCourt(request, ownerId);
        return ResponseHelper.created(courtDTO);
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<ApiResponse<List<CourtDTO>>> getVenueCourts(
            @PathVariable Long venueId) {
        List<CourtDTO> courts = venueService.getVenueCourts(venueId);
        return ResponseHelper.ok(courts);
    }

    @GetMapping("/{courtId}")
    public ResponseEntity<ApiResponse<DetailedCourtDTO>> getCourtById(
            @PathVariable Long courtId) {
        DetailedCourtDTO court = venueService.getDetailedCourtById(courtId);
        return ResponseHelper.ok(court);
    }

    @GetMapping("/venue/{venueId}/active")
    public ResponseEntity<ApiResponse<List<CourtDTO>>> getActiveVenueCourts(
            @PathVariable Long venueId) {
        List<CourtDTO> courts = venueService.getActiveCourts(venueId);
        return ResponseHelper.ok(courts);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CourtDTO>>> getAllActiveCourts() {
        List<CourtDTO> courts = venueService.getAllActiveCourts();
        return ResponseHelper.ok(courts);
    }

    @PutMapping("/{courtId}/activate")
    public ResponseEntity<ApiResponse<CourtDTO>> activateCourt(
            @PathVariable Long courtId,
            @RequestParam Long ownerId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        CourtDTO courtDTO = venueService.activateCourt(courtId, ownerId, isAdmin);
        return ResponseHelper.ok(courtDTO, "Court đã được kích hoạt");
    }

    @PutMapping("/{courtId}/deactivate")
    public ResponseEntity<ApiResponse<CourtDTO>> deactivateCourt(
            @PathVariable Long courtId,
            @RequestParam Long ownerId,
            @RequestParam(defaultValue = "false") boolean isAdmin) {
        CourtDTO courtDTO = venueService.deactivateCourt(courtId, ownerId, isAdmin);
        return ResponseHelper.ok(courtDTO, "Court đã được vô hiệu hóa");
    }
}

