package com.pickleball.application.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
}