package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.Player;

import java.util.Optional;

public interface PlayerRepository {
    Player save(Player player);
    Optional<Player> findByUserId(Long userId);
    Optional<Player> findByEmail(String email);
}
