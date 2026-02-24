package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.TimeSlotDTO;
import com.pickleball.application.services.TimeSlotApplicationService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/courts/{courtId}/slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotApplicationService timeSlotService;

    // Lấy các slots còn trống để đặt - dành cho user
    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getAvailableSlots(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TimeSlotDTO> slots = timeSlotService.getAvailableSlots(courtId, date);
        return ResponseHelper.ok(slots);
    }

    /**
     * Lấy tất cả slots (bao gồm BOOKED) - dành cho owner/admin
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getAllSlots(
            @PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TimeSlotDTO> slots = timeSlotService.getAllSlots(courtId, date);
        return ResponseHelper.ok(slots);
    }
}

