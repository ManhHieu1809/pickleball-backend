package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.UserDTO;
import com.pickleball.application.dtos.requests.ChangePasswordRequest;
import com.pickleball.application.dtos.requests.UpdateUserProfileRequest;
import com.pickleball.application.usecases.user.ChangePasswordUseCase;
import com.pickleball.application.usecases.user.GetUserProfileUseCase;
import com.pickleball.application.usecases.user.UpdateUserProfileUseCase;
import com.pickleball.domain.entities.User;
import com.pickleball.infrastructure.security.JwtService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final JwtService jwtService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserProfile(@PathVariable Long userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseHelper.badRequest("ID người dùng không hợp lệ");
            }

            User user = getUserProfileUseCase.execute(userId);
            return ResponseHelper.ok(convertToDTO(user));
        } catch (IllegalArgumentException e) {
            return ResponseHelper.notFound("Không tìm thấy người dùng có ID: " + userId);
        } catch (Exception e) {
            return ResponseHelper.internalError("Không thể truy xuất hồ sơ người dùng: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserProfileRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Xác thực quyền: chỉ user tự cập nhật
            Long currentUserId = extractUserIdFromToken(authHeader);
            if (!currentUserId.equals(userId)) {
                return ResponseHelper.forbidden("Bạn không có quyền cập nhật thông tin người dùng này");
            }

            User updatedUser = updateUserProfileUseCase.execute(
                    userId,
                    request.getFullName(),
                    request.getPhoneNumber(),
                    request.getProfilePictureUrl()
            );
            return ResponseHelper.ok(convertToDTO(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseHelper.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseHelper.internalError("Không thể cập nhật thông tin người dùng: " + e.getMessage());
        }
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Xác thực quyền: chỉ user tự đổi mật khẩu
            Long currentUserId = extractUserIdFromToken(authHeader);
            if (!currentUserId.equals(userId)) {
                return ResponseHelper.forbidden("Bạn không có quyền thay đổi mật khẩu của người dùng này");
            }

            changePasswordUseCase.execute(
                    userId,
                    request.getOldPassword(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );
            return ResponseHelper.ok("Đổi mật khẩu thành công");
        } catch (IllegalArgumentException e) {
            return ResponseHelper.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseHelper.internalError("Không thể thay đổi mật khẩu: " + e.getMessage());
        }
    }

    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}