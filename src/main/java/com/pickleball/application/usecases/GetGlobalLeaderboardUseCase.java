package com.pickleball.application.usecases;

import com.pickleball.application.dtos.LeaderboardEntryDTO;
import com.pickleball.application.dtos.LeaderboardPageDTO;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.repositories.PlayerRepository;

import java.util.ArrayList;
import java.util.List;

public class GetGlobalLeaderboardUseCase {

    private final PlayerRepository playerRepository;

    public GetGlobalLeaderboardUseCase(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public LeaderboardPageDTO execute(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;

        List<Player> topPlayers = playerRepository.findTopPlayers(page, size);
        long totalElements = playerRepository.countTotalPlayers();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<LeaderboardEntryDTO> entries = new ArrayList<>();
        int startIndex = page * size;

        for (int i = 0; i < topPlayers.size(); i++) {
            Player player = topPlayers.get(i);
            int rank = startIndex + i + 1;

            String fullName = null;
            String avatarUrl = null;
            if (player.getUser() != null) {
                fullName = player.getUser().getFullName();
                avatarUrl = player.getUser().getProfilePictureUrl();
            }

            if (fullName == null || fullName.trim().isEmpty()) {
                fullName = "Player #" + player.getUserId();
            }

            entries.add(LeaderboardEntryDTO.builder()
                    .rank(rank)
                    .playerId(player.getUserId())
                    .fullName(fullName)
                    .avatarUrl(avatarUrl)
                    .currentElo(player.getCurrentElo())
                    .loyaltyTier(player.getLoyaltyTier() != null ? player.getLoyaltyTier().name() : null)
                    .build());
        }

        return LeaderboardPageDTO.builder()
                .content(entries)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(page)
                .build();
    }
}

