package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.User;
import com.pickleball.infrastructure.persistence.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(User domainUser) {
        if (domainUser == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(domainUser.getId());
        entity.setEmail(domainUser.getEmail());
        entity.setPasswordHash(domainUser.getPasswordHash());
        entity.setFullName(domainUser.getFullName());
        entity.setPhoneNumber(domainUser.getPhoneNumber());
        entity.setProfilePictureUrl(domainUser.getProfilePictureUrl());
        entity.setCreatedAt(domainUser.getCreatedAt());

        return entity;
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        User domainUser = User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .fullName(entity.getFullName())
                .phoneNumber(entity.getPhoneNumber())
                .profilePictureUrl(entity.getProfilePictureUrl())
                .createdAt(entity.getCreatedAt())
                .build();

        return domainUser;
    }
}