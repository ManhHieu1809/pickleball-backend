package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.*;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.*;
import com.pickleball.domain.services.EloCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        // 1. Get Ranked Match and validate status
        RankedMatch match = rankedMatchRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Ranked match not found for booking: " + bookingId));

        if (match.getStatus() != MatchStatus.CONFIRMED && match.getStatus() != MatchStatus.RESOLVED) {
            throw new IllegalStateException("Elo can only be updated for CONFIRMED or RESOLVED matches. Current status: " + match.getStatus());
        }

        // Check if Elo already updated (prevent double update)
        // A simple check is to see if any EloHistory exists for this match
        List<EloHistory> existingHistory = eloHistoryRepository.findByRankedMatchId(match.getId());
        if (!existingHistory.isEmpty()) {
            throw new IllegalStateException("Elo has already been updated for this match");
        }

        // 2. Get Booking and Participants
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        List<BookingParticipant> participants = booking.getParticipants().stream()
                .filter(p -> p.getRole() == ParticipantRole.PLAYER)
                .collect(Collectors.toList());

        // 3. Separate into Teams and fetch Players
        List<Player> teamAPlayers = new ArrayList<>();
        List<Player> teamBPlayers = new ArrayList<>();

        for (BookingParticipant participant : participants) {
            Player player = playerRepository.findByUserId(participant.getUserId())
                    .orElseThrow(() -> new RuntimeException("Player not found: " + participant.getUserId()));

            if ("A".equals(participant.getTeam())) {
                teamAPlayers.add(player);
            } else if ("B".equals(participant.getTeam())) {
                teamBPlayers.add(player);
            }
        }

        if (teamAPlayers.isEmpty() || teamBPlayers.isEmpty()) {
            throw new IllegalStateException("Teams are not properly assigned in booking");
        }

        // 4. Calculate Elo Changes
        Map<Long, EloCalculationService.EloChangeResult> eloChanges = eloCalculationService.calculateEloChanges(
                teamAPlayers, teamBPlayers, match.getWinningTeam());

        // 5. Update Players and Save History
        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(teamAPlayers);
        allPlayers.addAll(teamBPlayers);

        for (Player player : allPlayers) {
            EloCalculationService.EloChangeResult result = eloChanges.get(player.getUserId());
            if (result != null) {
                // Save Elo History
                EloHistory eloHistory = EloHistory.builder()
                        .userId(player.getUserId())
                        .rankedMatchId(match.getId())
                        .seasonId(match.getSeasonId())
                        .eloBefore(result.getEloBefore())
                        .eloChange(result.getEloChange())
                        .eloAfter(result.getEloAfter())
                        .build();
                eloHistoryRepository.save(eloHistory);

                // Update Player
                player.updateElo(result.getEloAfter());
                playerRepository.save(player);

                // Save Skill Rating History (Snapshot - no change logic implemented yet)
                SkillRatingHistory skillHistory = SkillRatingHistory.builder()
                        .playerId(player.getUserId())
                        .matchId(match.getId())
                        .seasonId(match.getSeasonId())
                        .muBefore(player.getRatingMu())
                        .sigmaBefore(player.getRatingSigma())
                        .muAfter(player.getRatingMu()) // No change for now
                        .sigmaAfter(player.getRatingSigma()) // No change for now
                        .build();
                skillRatingHistoryRepository.save(skillHistory);
            }
        }
    }
}
