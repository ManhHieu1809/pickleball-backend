package com.pickleball.application.usecases.player;

import com.pickleball.application.dtos.PlayerMatchDTO;
import com.pickleball.domain.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetPlayerProfileUseCase {

    private final PlayerRepository playerRepository;

    public PlayerMatchDTO execute(Long userId) {
        return playerRepository.findByUserId(userId).map(player -> {
            PlayerMatchDTO dto = new PlayerMatchDTO();
            dto.setUserId(player.getUserId());
            dto.setCurrentElo(player.getCurrentElo());
            dto.setLoyaltyTier(player.getLoyaltyTier() != null ? player.getLoyaltyTier().name() : null);
            return dto;
        }).orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }
}
