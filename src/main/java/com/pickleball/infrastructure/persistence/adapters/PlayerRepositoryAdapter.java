package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.Player;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.infrastructure.persistence.entities.PlayerEntity;
import com.pickleball.infrastructure.persistence.mappers.PlayerMapper;
import com.pickleball.infrastructure.persistence.repositories.PlayerJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PlayerRepositoryAdapter implements PlayerRepository {
    private final PlayerJpaRepository playerJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PlayerMapper playerMapper;

    public PlayerRepositoryAdapter(PlayerJpaRepository playerJpaRepository,
                                   UserJpaRepository userJpaRepository,
                                   PlayerMapper playerMapper) {
        this.playerJpaRepository = playerJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.playerMapper = playerMapper;
    }

    @Override
    public Player save(Player player) {
        try {
            PlayerEntity entity = playerMapper.toEntity(player);
            PlayerEntity savedEntity = playerJpaRepository.save(entity);
            return playerMapper.toDomain(savedEntity);
        } catch (Exception e) {
            // If it's a constraint violation, provide a more user-friendly message
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                throw new IllegalArgumentException("Player profile already exists for this user");
            }
            throw e;
        }
    }

    @Override
    public Optional<Player> findByUserId(Long userId) {
        Optional<PlayerEntity> entity = playerJpaRepository.findByUserId(userId);
        return entity.map(playerMapper::toDomain);
    }

    @Override
    public Optional<Player> findByEmail(String email) {
        // Tìm User trước, sau đó tìm Player bằng userId
        return userJpaRepository.findByEmail(email)
                .flatMap(userEntity -> playerJpaRepository.findByUserId(userEntity.getId()))
                .map(playerMapper::toDomain);
    }
}
