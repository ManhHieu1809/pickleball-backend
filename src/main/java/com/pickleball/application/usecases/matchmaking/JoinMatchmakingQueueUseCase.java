package com.pickleball.application.usecases.matchmaking;

import com.pickleball.domain.entities.MatchmakingTicket;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.MatchmakingTicketRepository;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.domain.repositories.RefereeRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public class JoinMatchmakingQueueUseCase {

    private final MatchmakingTicketRepository matchmakingTicketRepository;
    private final PlayerRepository playerRepository;
    private final RefereeRepository refereeRepository;

    public JoinMatchmakingQueueUseCase(MatchmakingTicketRepository matchmakingTicketRepository,
                                       PlayerRepository playerRepository,
                                       RefereeRepository refereeRepository) {
        this.matchmakingTicketRepository = matchmakingTicketRepository;
        this.playerRepository = playerRepository;
        this.refereeRepository = refereeRepository;
    }

    public MatchmakingTicket execute(Long userId, ParticipantRole role, Double latitude, Double longitude) {
        // Check if user is already in queue
        Optional<MatchmakingTicket> existingTicket = matchmakingTicketRepository.findByUserIdAndIsActiveTrue(userId);
        if (existingTicket.isPresent()) {
            throw new IllegalArgumentException("User is already in the matchmaking queue");
        }

        int elo = 1200; // Default or fallback
        if (role == ParticipantRole.PLAYER) {
            Player player = playerRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Player profile not found"));
            elo = player.getCurrentElo();
        } else if (role == ParticipantRole.REFEREE) {
            com.pickleball.domain.entities.Referee referee = refereeRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User is not a registered referee"));
            if (!referee.isEligibleForMatch()) {
                throw new IllegalArgumentException("Referee is not eligible to join matches");
            }
        }

        // Create new ticket
        MatchmakingTicket newTicket = MatchmakingTicket.builder()
                .userId(userId)
                .role(role)
                .latitude(latitude)
                .longitude(longitude)
                .elo(elo)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        return matchmakingTicketRepository.save(newTicket);
    }
}

