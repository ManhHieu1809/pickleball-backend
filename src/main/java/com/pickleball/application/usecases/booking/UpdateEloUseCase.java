package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.*;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.*;
import com.pickleball.domain.services.EloCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UpdateEloUseCase {

    private final RankedMatchRepository rankedMatchRepository;
    private final BookingRepository bookingRepository;
    private final PlayerRepository playerRepository;
    private final EloHistoryRepository eloHistoryRepository;
    private final SkillRatingHistoryRepository skillRatingHistoryRepository;
    private final EloCalculationService eloCalculationService;

    @Transactional
    public void execute(Long bookingId) {
        RankedMatch match = rankedMatchRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Ranked match not found for booking: " + bookingId));

        if (match.getStatus() != MatchStatus.CONFIRMED && match.getStatus() != MatchStatus.RESOLVED) {
            throw new IllegalStateException("Elo can only be updated for CONFIRMED or RESOLVED matches. Current status: " + match.getStatus());
        }

        List<EloHistory> existingHistory = eloHistoryRepository.findByRankedMatchId(match.getId());
        if (!existingHistory.isEmpty()) {
            throw new IllegalStateException("Elo has already been updated for this match");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        List<BookingParticipant> playerParticipants = booking.getParticipants().stream()
                .filter(p -> p.getRole() != ParticipantRole.REFEREE)
                .collect(Collectors.toList());

        List<Player> teamAPlayers = new ArrayList<>();
        List<Player> teamBPlayers = new ArrayList<>();
        Map<Long, Integer> rankedMatchesPlayed = new HashMap<>();
        Map<Long, Integer> recentRepeatCounts = new HashMap<>();
        List<Long> currentPlayerIds = playerParticipants.stream()
                .map(BookingParticipant::getUserId)
                .collect(Collectors.toList());

        for (BookingParticipant participant : playerParticipants) {
            Player player = playerRepository.findByUserId(participant.getUserId())
                    .orElseThrow(() -> new RuntimeException("Player not found: " + participant.getUserId()));

            rankedMatchesPlayed.put(player.getUserId(), eloHistoryRepository.findByUserId(player.getUserId()).size());
            recentRepeatCounts.put(player.getUserId(), countRecentRepeats(player.getUserId(), currentPlayerIds));

            if ("A".equals(participant.getTeam())) {
                teamAPlayers.add(player);
            } else if ("B".equals(participant.getTeam())) {
                teamBPlayers.add(player);
            }
        }

        if (teamAPlayers.isEmpty() || teamBPlayers.isEmpty()) {
            throw new IllegalStateException("Teams are not properly assigned in booking");
        }

        Map<Long, EloCalculationService.EloChangeResult> eloChanges = eloCalculationService.calculateEloChanges(
                teamAPlayers, teamBPlayers, match.getWinningTeam(), rankedMatchesPlayed, recentRepeatCounts);

        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(teamAPlayers);
        allPlayers.addAll(teamBPlayers);

        for (Player player : allPlayers) {
            EloCalculationService.EloChangeResult result = eloChanges.get(player.getUserId());
            if (result != null) {
                EloHistory eloHistory = EloHistory.builder()
                        .userId(player.getUserId())
                        .rankedMatchId(match.getId())
                        .seasonId(match.getSeasonId())
                        .eloBefore(result.getEloBefore())
                        .eloChange(result.getEloChange())
                        .eloAfter(result.getEloAfter())
                        .build();
                eloHistoryRepository.save(eloHistory);

                player.setRatingMu(result.getMuAfter());
                player.setRatingSigma(result.getSigmaAfter());
                player.updateElo(result.getEloAfter());
                playerRepository.save(player);

                SkillRatingHistory skillHistory = SkillRatingHistory.builder()
                        .playerId(player.getUserId())
                        .matchId(match.getId())
                        .seasonId(match.getSeasonId())
                        .muBefore(result.getMuBefore())
                        .sigmaBefore(result.getSigmaBefore())
                        .muAfter(result.getMuAfter())
                        .sigmaAfter(result.getSigmaAfter())
                        .build();
                skillRatingHistoryRepository.save(skillHistory);
            }
        }
    }

    private int countRecentRepeats(Long userId, List<Long> currentPlayerIds) {
        List<Long> recentOpponentIds = bookingRepository.findRecentOpponentUserIds(userId, 3);
        return (int) currentPlayerIds.stream()
                .filter(otherUserId -> !otherUserId.equals(userId))
                .filter(recentOpponentIds::contains)
                .count();
    }
}
