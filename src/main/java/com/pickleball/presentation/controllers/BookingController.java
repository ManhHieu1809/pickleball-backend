// File: presentation/controllers/BookingController.java
package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.BookingDTO;
import com.pickleball.application.dtos.CasualMatchDTO;
import com.pickleball.application.dtos.PlayerMatchDTO;
import com.pickleball.application.dtos.requests.CreateBookingRequest;
import com.pickleball.application.dtos.requests.JoinBookingRequest;
import com.pickleball.application.services.BookingApplicationService;
import com.pickleball.presentation.responses.ApiResponse;
import com.pickleball.presentation.helpers.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingApplicationService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingDTO bookingDTO = bookingService.createBooking(request);
        return ResponseHelper.created(bookingDTO);
    }

    /**
     * Create Casual Match - returns booking + candidates + deposit info
     * POST /api/bookings/casual
     */
    @PostMapping("/casual")
    public ResponseEntity<ApiResponse<CasualMatchDTO>> createCasualMatch(
            @Valid @RequestBody CreateBookingRequest request) {
        request.setBookingType(com.pickleball.domain.enums.BookingType.CASUAL);
        CasualMatchDTO casualMatch = bookingService.createCasualMatch(request);
        return ResponseHelper.created(casualMatch, "Casual match created. Waiting for players to join.");
    }

    /**
     * Get available casual matches (PENDING) for players to browse
     * GET /api/bookings/casual/available
     */
    @GetMapping("/casual/available")
    public ResponseEntity<ApiResponse<List<CasualMatchDTO>>> getAvailableCasualMatches() {
        List<CasualMatchDTO> matches = bookingService.getAvailableCasualMatches();
        return ResponseHelper.ok(matches);
    }

    /**
     * Get matching candidates for an existing casual match
     * GET /api/bookings/{bookingId}/candidates
     */
    @GetMapping("/{bookingId}/candidates")
    public ResponseEntity<ApiResponse<List<PlayerMatchDTO>>> getCandidates(
            @PathVariable Long bookingId) {
        List<PlayerMatchDTO> candidates = bookingService.getCasualMatchCandidates(bookingId);
        return ResponseHelper.ok(candidates, "Found " + candidates.size() + " matching candidates");
    }

    @PostMapping("/{bookingId}/join")
    public ResponseEntity<ApiResponse<BookingDTO>> joinBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody JoinBookingRequest request) {
        BookingDTO bookingDTO = bookingService.joinBooking(bookingId, request);
        return ResponseHelper.ok(bookingDTO, "Joined booking successfully");
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam Long userId) {
        BookingDTO bookingDTO = bookingService.cancelBooking(bookingId, userId);
        return ResponseHelper.ok(bookingDTO, "Booking cancelled successfully");
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<BookingDTO>> getBooking(@PathVariable Long bookingId) {
        BookingDTO bookingDTO = bookingService.getBooking(bookingId);
        return ResponseHelper.ok(bookingDTO);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getMyBookings(
            @RequestParam Long userId) {
        List<BookingDTO> bookings = bookingService.getMyBookings(userId);
        return ResponseHelper.ok(bookings);
    }
}