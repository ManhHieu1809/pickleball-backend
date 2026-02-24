package com.pickleball.application.usecases.user;

import com.pickleball.domain.entities.User;
import com.pickleball.domain.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LoginUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if(!passwordEncoder.matches(password, user.getPasswordHash())){
            throw new IllegalArgumentException("Invalid password");
        }
        return user;
    }
}
