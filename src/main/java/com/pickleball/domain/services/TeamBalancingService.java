package com.pickleball.domain.services;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.PlayerRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        if (balancePartyTeams(players)) {
            return;
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

    private boolean balancePartyTeams(List<BookingParticipant> players) {
        Map<Long, List<BookingParticipant>> partyGroups = players.stream()
                .filter(player -> player.getPartyId() != null)
                .collect(Collectors.groupingBy(
                        BookingParticipant::getPartyId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        boolean hasDuo = partyGroups.values().stream().anyMatch(group -> group.size() == 2);
        if (!hasDuo || partyGroups.values().stream().anyMatch(group -> group.size() > 2)) {
            return false;
        }

        List<List<BookingParticipant>> units = new ArrayList<>();
        partyGroups.values().stream()
                .filter(group -> group.size() == 2)
                .forEach(units::add);

        players.stream()
                .filter(player -> player.getPartyId() == null || partyGroups.get(player.getPartyId()).size() == 1)
                .map(List::of)
                .forEach(units::add);

        List<BookingParticipant> teamA = new ArrayList<>();
        List<BookingParticipant> teamB = new ArrayList<>();

        for (List<BookingParticipant> unit : units) {
            if (teamA.size() <= teamB.size() && teamA.size() + unit.size() <= 2) {
                teamA.addAll(unit);
            } else if (teamB.size() + unit.size() <= 2) {
                teamB.addAll(unit);
            } else if (teamA.size() + unit.size() <= 2) {
                teamA.addAll(unit);
            } else {
                return false;
            }
        }

        if (teamA.size() != 2 || teamB.size() != 2) {
            return false;
        }

        teamA.forEach(player -> player.setTeam("A"));
        teamB.forEach(player -> player.setTeam("B"));
        return true;
    }

    private record PlayerParticipantInfo(BookingParticipant participant, int elo) {}
}
