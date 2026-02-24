package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.AuthenticationResponse;
import com.pickleball.application.dtos.CreateUserRequest;
import com.pickleball.application.dtos.LoginRequest;
import com.pickleball.application.dtos.RefreshTokenRequest;
import com.pickleball.application.dtos.UserDTO;
import com.pickleball.application.services.AuthApplicationService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody CreateUserRequest request) {
        AuthenticationResponse response = authService.register(request);
        return ResponseHelper.created(response, "Đăng ký thành công");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = authService.login(request);
        return ResponseHelper.ok(response, "Đăng nhập thành công");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseHelper.ok(response, "Làm mới token thành công");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UserDTO user = authService.getCurrentUser(token);
        return ResponseHelper.ok(user);
    }
}
