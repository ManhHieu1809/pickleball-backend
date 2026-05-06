package com.pickleball.infrastructure.security;

import com.pickleball.domain.entities.User;
import com.pickleball.domain.entities.VenueStaff;
import com.pickleball.domain.repositories.UserRepository;
import com.pickleball.domain.repositories.VenueStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final VenueStaffRepository venueStaffRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPasswordHash(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        }

        Optional<VenueStaff> staffOpt = venueStaffRepository.findByUsername(username);
        if (staffOpt.isPresent()) {
            VenueStaff staff = staffOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    staff.getUsername(),
                    staff.getPasswordHash(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF"))
            );
        }

        throw new UsernameNotFoundException("User not found with email or username: " + username);
    }

    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
