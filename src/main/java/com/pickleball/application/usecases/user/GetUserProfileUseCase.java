package com.pickleball.application.usecases.user;

import com.pickleball.domain.entities.User;
import com.pickleball.domain.repositories.UserRepository;

public class GetUserProfileUseCase {
    private final UserRepository userRepository;

    public GetUserProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
