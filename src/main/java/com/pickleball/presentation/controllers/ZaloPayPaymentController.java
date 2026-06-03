package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.ZaloPayPaymentDTO;
import com.pickleball.application.dtos.requests.CreateZaloPayTopUpRequest;
import com.pickleball.application.services.ZaloPayWalletTopUpService;
import com.pickleball.infrastructure.security.JwtService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ZaloPayPaymentController {
    private final ZaloPayWalletTopUpService zaloPayWalletTopUpService;
    private final JwtService jwtService;

    @PostMapping("/api/payments/zalopay/topup")
    public ResponseEntity<ApiResponse<ZaloPayPaymentDTO>> createTopUp(
            @Valid @RequestBody CreateZaloPayTopUpRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserIdFromToken(authHeader);
        ZaloPayPaymentDTO payment = zaloPayWalletTopUpService.createTopUp(
                userId,
                request.getAmount(),
                request.getDescription());
        return ResponseHelper.created(payment, "ZaloPay payment order created");
    }

    @GetMapping("/api/payments/zalopay/status/{appTransId}")
    public ResponseEntity<ApiResponse<ZaloPayPaymentDTO>> getStatus(@PathVariable String appTransId) {
        return ResponseHelper.ok(zaloPayWalletTopUpService.getLocalStatus(appTransId));
    }

    @PostMapping("/api/payments/zalopay/status/{appTransId}/sync")
    public ResponseEntity<ApiResponse<ZaloPayPaymentDTO>> syncStatus(@PathVariable String appTransId) {
        return ResponseHelper.ok(zaloPayWalletTopUpService.syncStatus(appTransId), "ZaloPay status synchronized");
    }

    @PostMapping(value = {"/payment/callback", "/api/payments/zalopay/callback"})
    public ResponseEntity<Map<String, Object>> callback(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            String data = body.get("data") == null ? null : String.valueOf(body.get("data"));
            String mac = body.get("mac") == null ? null : String.valueOf(body.get("mac"));
            zaloPayWalletTopUpService.completeFromCallback(data, mac);
            result.put("return_code", 1);
            result.put("return_message", "success");
        } catch (SecurityException e) {
            result.put("return_code", -1);
            result.put("return_message", e.getMessage());
        } catch (Exception e) {
            result.put("return_code", 0);
            result.put("return_message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/payment/result", produces = MediaType.TEXT_HTML_VALUE)
    public String resultPage() {
        return """
                <!doctype html>
                <html>
                <head><meta charset="utf-8"><title>Payment Result</title></head>
                <body>
                  <h3>Payment processing</h3>
                  <p>You can return to the app and refresh the wallet balance.</p>
                </body>
                </html>
                """;
    }

    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token khong hop le");
        }
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
