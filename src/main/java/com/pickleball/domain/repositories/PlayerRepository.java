package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {
    Player save(Player player);
    Optional<Player> findByUserId(Long userId);
    Optional<Player> findByEmail(String email);
    List<Player> findByEloRange(int minElo, int maxElo);
    List<Player> findByUserIdIn(List<Long> userIds);
    void updateLocation(Long userId, Double latitude, Double longitude);
    List<Player> findTopPlayers(int page, int size);
    long countTotalPlayers();
}
