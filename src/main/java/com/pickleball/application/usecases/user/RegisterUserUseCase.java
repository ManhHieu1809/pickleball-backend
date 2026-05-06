package com.pickleball.application.usecases.user;

import com.pickleball.domain.entities.User;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.repositories.UserRepository;
import com.pickleball.domain.repositories.PlayerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository,
                               PlayerRepository playerRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = Exception.class)
    public User execute(User user) {
        try {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }

            if (user.getPhoneNumber() != null &&
                    userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
                throw new IllegalArgumentException("Phone number already exists");
            }

            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

            User savedUser = userRepository.save(user);

            if (savedUser.getId() != null) {
                if (playerRepository.findByUserId(savedUser.getId()).isEmpty()) {
                    try {
                        Player player = Player.builder()
                                .userId(savedUser.getId())
                                .build();
                        playerRepository.save(player);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create player profile: " + e.getMessage(), e);
                    }
                }
            } else {
                throw new RuntimeException("Failed to save user - no ID generated");
            }

            return savedUser;
        } catch (Exception e) {
            throw e;
        }
    }
}