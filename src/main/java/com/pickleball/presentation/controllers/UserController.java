package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.UserDTO;
import com.pickleball.application.usecases.user.GetUserProfileUseCase;
import com.pickleball.domain.entities.User;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;

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