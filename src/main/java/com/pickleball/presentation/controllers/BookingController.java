package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.*;
import com.pickleball.application.dtos.requests.ConfirmResultRequest;
import com.pickleball.application.dtos.requests.SubmitResultRequest;
import com.pickleball.application.dtos.requests.CheckInRequest;
import com.pickleball.application.dtos.requests.CreateBookingRequest;
import com.pickleball.application.dtos.requests.JoinBookingRequest;
import com.pickleball.application.dtos.requests.SubmitDisputeRequest;
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

    @PostMapping("/casual")
    public ResponseEntity<ApiResponse<CasualMatchDTO>> createCasualMatch(
            @Valid @RequestBody CreateBookingRequest request) {
        request.setBookingType(com.pickleball.domain.enums.BookingType.CASUAL);
        CasualMatchDTO casualMatch = bookingService.createCasualMatch(request);
        return ResponseHelper.created(casualMatch, "Casual match created. Waiting for players to join.");
    }

    @GetMapping("/casual/available")
    public ResponseEntity<ApiResponse<List<CasualMatchDTO>>> getAvailableCasualMatches() {
        List<CasualMatchDTO> matches = bookingService.getAvailableCasualMatches();
        return ResponseHelper.ok(matches);
    }

    @PostMapping("/ranked")
    public ResponseEntity<ApiResponse<RankedMatchDTO>> createRankedMatch(
            @Valid @RequestBody CreateBookingRequest request) {
        request.setBookingType(com.pickleball.domain.enums.BookingType.RANKED);
        RankedMatchDTO rankedMatch = bookingService.createRankedMatch(request);
        return ResponseHelper.created(rankedMatch, "Ranked match created. Waiting for players and referee to join.");
    }

    @GetMapping("/ranked/available")
    public ResponseEntity<ApiResponse<List<RankedMatchDTO>>> getAvailableRankedMatches() {
        List<RankedMatchDTO> matches = bookingService.getAvailableRankedMatches();
        return ResponseHelper.ok(matches);
    }

    @GetMapping("/{bookingId}/ranked-candidates")
    public ResponseEntity<ApiResponse<RankedMatchDTO>> getRankedMatchCandidates(
            @PathVariable Long bookingId) {
        RankedMatchDTO candidates = bookingService.getRankedMatchCandidates(bookingId);
        return ResponseHelper.ok(candidates, "Ranked match candidates retrieved");
    }

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

    @PostMapping("/{bookingId}/accept-match")
    public ResponseEntity<ApiResponse<BookingDTO>> acceptMatch(
            @PathVariable Long bookingId,
            @RequestParam Long userId) {
        BookingDTO bookingDTO = bookingService.acceptMatch(bookingId, userId);
        return ResponseHelper.ok(bookingDTO, "Match accepted and deposit paid successfully");
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

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getOwnerBookings(@PathVariable Long ownerId) {
        List<BookingDTO> bookings = bookingService.getOwnerBookings(ownerId);
        return ResponseHelper.ok(bookings);
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getVenueBookings(@PathVariable Long venueId) {
        List<BookingDTO> bookings = bookingService.getVenueBookings(venueId);
        return ResponseHelper.ok(bookings);
    }


    @PostMapping("/{bookingId}/check-in")
    public ResponseEntity<ApiResponse<Void>> checkIn(
            @PathVariable Long bookingId,
            @Valid @RequestBody CheckInRequest request) {
        bookingService.checkIn(bookingId, request);
        return ResponseHelper.ok(null, "Check-in successful");
    }

    @PostMapping("/{bookingId}/submit-result")
    public ResponseEntity<ApiResponse<Void>> submitMatchResult(
            @PathVariable Long bookingId,
            @Valid @RequestBody SubmitResultRequest request) {
        bookingService.submitMatchResult(bookingId, request);
        return ResponseHelper.ok(null, "Match result submitted successfully");
    }

    @PostMapping("/{bookingId}/confirm-result")
    public ResponseEntity<ApiResponse<Void>> confirmMatchResult(
            @PathVariable Long bookingId,
            @Valid @RequestBody ConfirmResultRequest request) {
        bookingService.confirmMatchResult(bookingId, request);
        return ResponseHelper.ok(null, "Result confirmed");
    }

    @PostMapping("/{bookingId}/disputes")
    public ResponseEntity<ApiResponse<MatchDisputeDTO>> submitDispute(
            @PathVariable Long bookingId,
            @Valid @RequestBody SubmitDisputeRequest request) {
        MatchDisputeDTO dispute = bookingService.submitDispute(request);
        return ResponseHelper.created(dispute);
    }
}