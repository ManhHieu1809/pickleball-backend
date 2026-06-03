package com.pickleball.application.services;

import com.pickleball.application.dtos.PlayerCareerStatsDTO;
import com.pickleball.application.dtos.PlayerSearchResultDTO;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import com.pickleball.infrastructure.persistence.entities.EloHistoryEntity;
import com.pickleball.infrastructure.persistence.entities.PlayerEntity;
import com.pickleball.infrastructure.persistence.entities.UserEntity;
import com.pickleball.infrastructure.persistence.repositories.EloHistoryJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.PlayerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerQueryApplicationService {

    private final PlayerJpaRepository playerJpaRepository;
    private final BookingRepository bookingRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final EloHistoryJpaRepository eloHistoryJpaRepository;

    @Transactional(readOnly = true)
    public List<PlayerSearchResultDTO> searchPlayers(String query, int limit) {
        String normalizedQuery = query == null ? "" : query.trim();
        int safeLimit = Math.max(1, Math.min(limit, 50));

        return playerJpaRepository.searchPlayers(normalizedQuery, PageRequest.of(0, safeLimit)).stream()
                .map(this::toSearchResult)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlayerCareerStatsDTO getCareerStats(Long userId) {
        PlayerEntity player = playerJpaRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Player profile not found for user: " + userId));

        List<Booking> rankedBookings = bookingRepository.findByParticipantUserId(userId).stream()
                .filter(booking -> booking.getBookingType() == BookingType.RANKED)
                .sorted(Comparator.comparing(Booking::getStartTime).reversed())
                .collect(Collectors.toList());

        int totalWins = 0;
        int totalMatches = 0;
        int currentWinStreak = 0;
        boolean streakOpen = true;

        for (Booking booking : rankedBookings) {
            String userTeam = findUserTeam(booking, userId);
            if (userTeam == null) {
                continue;
            }

            RankedMatch rankedMatch = rankedMatchRepository.findByBookingId(booking.getId()).orElse(null);
            if (rankedMatch == null || rankedMatch.getWinningTeam() == null) {
                continue;
            }

            boolean won = rankedMatch.getWinningTeam().equals(userTeam);
            totalMatches++;
            if (won) {
                totalWins++;
                if (streakOpen) {
                    currentWinStreak++;
                }
            } else {
                streakOpen = false;
            }
        }

        int highestElo = Math.max(
                player.getCurrentElo() != null ? player.getCurrentElo() : 1000,
                eloHistoryJpaRepository.findByUserId(userId).stream()
                        .map(EloHistoryEntity::getEloAfter)
                        .filter(elo -> elo != null)
                        .max(Integer::compareTo)
                        .orElse(0));

        return PlayerCareerStatsDTO.builder()
                .userId(userId)
                .totalMvp(0)
                .highestRank(toRank(highestElo))
                .totalTournaments(0)
                .currentWinStreak(currentWinStreak)
                .totalWins(totalWins)
                .totalMatches(totalMatches)
                .build();
    }

    private PlayerSearchResultDTO toSearchResult(PlayerEntity player) {
        UserEntity user = player.getUser();
        String email = user != null ? user.getEmail() : null;
        return PlayerSearchResultDTO.builder()
                .userId(player.getUserId())
                .fullName(user != null ? user.getFullName() : null)
                .username(toUsername(email))
                .currentElo(player.getCurrentElo())
                .loyaltyTier(player.getLoyaltyTier() != null ? player.getLoyaltyTier().name() : null)
                .avatarUrl(user != null ? user.getProfilePictureUrl() : null)
                .build();
    }

    private String toUsername(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex).toLowerCase(Locale.ROOT) : email.toLowerCase(Locale.ROOT);
    }

    private String findUserTeam(Booking booking, Long userId) {
        if (booking.getParticipants() == null) {
            return null;
        }
        return booking.getParticipants().stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .filter(participant -> participant.getRole() == ParticipantRole.PLAYER || participant.getRole() == ParticipantRole.HOST)
                .map(BookingParticipant::getTeam)
                .filter(team -> team != null)
                .findFirst()
                .orElse(null);
    }

    private String toRank(int elo) {
        if (elo >= 2000) return "DIAMOND_I";
        if (elo >= 1850) return "DIAMOND_II";
        if (elo >= 1700) return "PLATINUM_I";
        if (elo >= 1550) return "PLATINUM_II";
        if (elo >= 1400) return "GOLD_I";
        if (elo >= 1250) return "GOLD_II";
        if (elo >= 1100) return "SILVER_I";
        if (elo >= 950) return "SILVER_II";
        return "BRONZE_I";
    }
}
