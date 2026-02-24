package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.BookingDTO;
import com.pickleball.application.dtos.VenueStaffDTO;
import com.pickleball.application.dtos.requests.CreateVenueStaffRequest;
import com.pickleball.application.dtos.requests.CreateWalkInBookingRequest;
import com.pickleball.application.dtos.requests.StaffLoginRequest;
import com.pickleball.application.services.VenueStaffApplicationService;
import com.pickleball.application.services.VenueStaffApplicationService.StaffLoginResponse;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class VenueStaffController {

    private final VenueStaffApplicationService staffService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<StaffLoginResponse>> login(
            @Valid @RequestBody StaffLoginRequest request) {
        StaffLoginResponse response = staffService.login(request);
        return ResponseHelper.ok(response, "Đăng nhập thành công");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VenueStaffDTO>> createStaff(
            @Valid @RequestBody CreateVenueStaffRequest request,
            @RequestParam Long ownerId) {
        VenueStaffDTO staff = staffService.createStaff(request, ownerId);
        return ResponseHelper.created(staff, "Tạo nhân viên thành công");
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<ApiResponse<List<VenueStaffDTO>>> getStaffByVenue(
            @PathVariable Long venueId,
            @RequestParam Long ownerId) {
        List<VenueStaffDTO> staffList = staffService.getStaffByVenue(venueId, ownerId);
        return ResponseHelper.ok(staffList);
    }

    @PutMapping("/{staffId}/deactivate")
    public ResponseEntity<ApiResponse<VenueStaffDTO>> deactivateStaff(
            @PathVariable Long staffId,
            @RequestParam Long ownerId) {
        VenueStaffDTO staff = staffService.deactivateStaff(staffId, ownerId);
        return ResponseHelper.ok(staff, "Đã vô hiệu hóa nhân viên");
    }

    @PutMapping("/{staffId}/activate")
    public ResponseEntity<ApiResponse<VenueStaffDTO>> activateStaff(
            @PathVariable Long staffId,
            @RequestParam Long ownerId) {
        VenueStaffDTO staff = staffService.activateStaff(staffId, ownerId);
        return ResponseHelper.ok(staff, "Đã kích hoạt nhân viên");
    }

    @PutMapping("/{staffId}/permissions")
    public ResponseEntity<ApiResponse<VenueStaffDTO>> updatePermissions(
            @PathVariable Long staffId,
            @RequestParam Long ownerId,
            @RequestBody Set<String> permissions) {
        VenueStaffDTO staff = staffService.updatePermissions(staffId, ownerId, permissions);
        return ResponseHelper.ok(staff, "Đã cập nhật quyền của nhân viên");
    }

    @PostMapping("/walk-in-booking")
    public ResponseEntity<ApiResponse<BookingDTO>> createWalkInBooking(
            @Valid @RequestBody CreateWalkInBookingRequest request,
            @RequestParam Long staffId) {
        BookingDTO booking = staffService.createWalkInBooking(staffId, request);
        return ResponseHelper.created(booking, "Tạo booking walk-in thành công");
    }
}
