package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.User;
import com.pickleball.domain.repositories.UserRepository;
import com.pickleball.infrastructure.persistence.entities.UserEntity;
import com.pickleball.infrastructure.persistence.mappers.UserMapper;
import com.pickleball.infrastructure.persistence.repositories.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository, UserMapper userMapper){
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email){
        Optional<UserEntity> entity = userJpaRepository.findByEmail(email);
        return entity.map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        Optional<UserEntity> entity = userJpaRepository.findByPhoneNumber(phoneNumber);
        return entity.map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userJpaRepository.existsByPhoneNumber(phoneNumber);
    }

}
