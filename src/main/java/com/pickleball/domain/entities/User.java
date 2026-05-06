package com.pickleball.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phoneNumber;
    private String profilePictureUrl;
    private LocalDateTime createdAt;

    public void updateProfile(String fullName, String phoneNumber, String profilePictureUrl) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName;
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            this.phoneNumber = phoneNumber;
        }
        if (profilePictureUrl != null) {
            this.profilePictureUrl = profilePictureUrl;
        }
    }

    public boolean isProfileComplete() {
        return email != null && !email.trim().isEmpty() &&
                fullName != null && !fullName.trim().isEmpty() &&
                phoneNumber != null && !phoneNumber.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}