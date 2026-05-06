package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.finance.OwnerFinanceOverviewDTO;
import com.pickleball.application.services.OwnerFinanceService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owner/finance")
@RequiredArgsConstructor
public class OwnerFinanceController {

    private final OwnerFinanceService ownerFinanceService;

    @GetMapping("/overview/{ownerId}")
    public ResponseEntity<ApiResponse<OwnerFinanceOverviewDTO>> getOverview(
            @PathVariable Long ownerId,
            @RequestParam(required = false) String period) {
        OwnerFinanceOverviewDTO overview = ownerFinanceService.getOverview(ownerId, period);
        return ResponseHelper.ok(overview);
    }
}
