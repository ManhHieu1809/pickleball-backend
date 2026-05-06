package com.pickleball.domain.services;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.PlayerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TeamBalancingService {

    private final PlayerRepository playerRepository;

    public TeamBalancingService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void balanceTeams(Booking booking) {
        if (booking.getBookingType() != BookingType.RANKED) {
            return;
        }

        List<BookingParticipant> players = booking.getParticipants().stream()
                .filter(p -> p.getRole() != ParticipantRole.REFEREE)
                .collect(Collectors.toList());

        if (players.size() != 4) {
            throw new IllegalStateException("Cannot balance teams: Expected exactly 4 players, found " + players.size());
        }

        List<PlayerParticipantInfo> playerInfos = players.stream()
                .map(p -> {
                    Player player = playerRepository.findByUserId(p.getUserId())
                            .orElseThrow(() -> new IllegalStateException("Player profile not found for user: " + p.getUserId()));
                    return new PlayerParticipantInfo(p, player.getCurrentElo());
                })
                .sorted(Comparator.comparingInt(PlayerParticipantInfo::elo).reversed())
                .collect(Collectors.toList());

        playerInfos.get(0).participant().setTeam("A");
        playerInfos.get(3).participant().setTeam("A");

        playerInfos.get(1).participant().setTeam("B");
        playerInfos.get(2).participant().setTeam("B");
    }

    private record PlayerParticipantInfo(BookingParticipant participant, int elo) {}
}
