// File: presentation/controllers/BookingController.java
package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.BookingDTO;
import com.pickleball.application.dtos.requests.CreateBookingRequest;
import com.pickleball.application.dtos.requests.JoinBookingRequest;
import com.pickleball.application.services.BookingApplicationService;
import com.pickleball.presentation.responses.ApiResponse;
import com.pickleball.presentation.helpers.ResponseHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}