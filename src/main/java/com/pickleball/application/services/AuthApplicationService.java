package com.pickleball.application.services;

import com.pickleball.application.dtos.AuthenticationResponse;
import com.pickleball.application.dtos.CreateUserRequest;
import com.pickleball.application.dtos.LoginRequest;
import com.pickleball.application.dtos.UserDTO;
import com.pickleball.application.usecases.user.LoginUserUseCase;
import com.pickleball.application.usecases.user.RegisterUserUseCase;
import com.pickleball.domain.entities.User;
import com.pickleball.domain.repositories.UserRepository;
import com.pickleball.infrastructure.persistence.repositories.AdminJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.PlayerJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.VenueOwnerJpaRepository;
import com.pickleball.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AdminJpaRepository adminJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final VenueOwnerJpaRepository venueOwnerJpaRepository;

    @Transactional
    public AuthenticationResponse register(CreateUserRequest request) {

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(request.getPassword()) // Will be encoded in use case
                .fullName(request.getFullName().trim())
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = registerUserUseCase.execute(user);

        String accessToken = jwtService.generateToken(savedUser.getId(), savedUser.getEmail());
        String refreshToken = jwtService.generateRefreshToken(savedUser.getId(), savedUser.getEmail());

        return AuthenticationResponse.of(
                accessToken,
                refreshToken,
                jwtService.getExpirationTime(),
                convertToDTO(savedUser)
        );
    }

    public AuthenticationResponse login(LoginRequest request) {
        User user = loginUserUseCase.execute(request.getEmail(), request.getPassword());

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        return AuthenticationResponse.of(
                accessToken,
                refreshToken,
                jwtService.getExpirationTime(),
                convertToDTO(user)
        );
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);

        if (email == null || jwtService.isTokenExpired(refreshToken)) {
            throw new IllegalArgumentException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        if (!jwtService.isTokenValid(refreshToken, user.getEmail())) {
            throw new IllegalArgumentException("Refresh token không hợp lệ");
        }

        String newAccessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        return AuthenticationResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtService.getExpirationTime(),
                convertToDTO(user)
        );
    }

    public UserDTO getCurrentUser(String token) {
        Long userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setCreatedAt(user.getCreatedAt());

        List<String> roles = new ArrayList<>();
        if (adminJpaRepository.existsByUserId(user.getId())) {
            roles.add("ADMIN");
        }
        if (venueOwnerJpaRepository.existsByUserId(user.getId())) {
            roles.add("VENUE_OWNER");
        }
        if (playerJpaRepository.existsByUserId(user.getId())) {
            roles.add("PLAYER");
        }
        if (roles.isEmpty()) {
            roles.add("PLAYER");
        }
        dto.setRoles(roles);

        return dto;
    }
}
