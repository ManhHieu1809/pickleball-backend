package com.pickleball.domain.services;

import com.pickleball.domain.entities.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MatchmakingService {

    private static final int DEFAULT_ELO_RANGE = 200;
    private static final int MAX_PLAYERS_PER_MATCH = 4;
    private static final int PLAYERS_NEEDED = 3;
    private static final int ANTI_REPETITION_LAST_N = 3;
    private static final double DEFAULT_RADIUS_KM = 15.0;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public List<Player> findMatchingPlayers(
            List<Player> candidates,
            Long hostUserId,
            List<Long> excludeUserIds,
            List<Long> recentOpponentIds,
            int hostElo,
            Double venueLat, Double venueLng, Double radiusKm,
            int maxResults) {

        final double radius = (radiusKm != null && radiusKm > 0) ? radiusKm : DEFAULT_RADIUS_KM;

        return candidates.stream()
                .filter(p -> !p.getUserId().equals(hostUserId))
                .filter(p -> !excludeUserIds.contains(p.getUserId()))
                .filter(p -> {
                    if (venueLat == null || venueLng == null) return true;
                    if (p.getLastLatitude() == null || p.getLastLongitude() == null) return true;
                    double distance = haversine(venueLat, venueLng, p.getLastLatitude(), p.getLastLongitude());
                    return distance <= radius;
                })
                .sorted((a, b) -> {
                    boolean aRecent = recentOpponentIds.contains(a.getUserId());
                    boolean bRecent = recentOpponentIds.contains(b.getUserId());
                    if (aRecent != bRecent) {
                        return aRecent ? 1 : -1;
                    }
                    int diffA = Math.abs(a.getCurrentElo() - hostElo);
                    int diffB = Math.abs(b.getCurrentElo() - hostElo);
                    return Integer.compare(diffA, diffB);
                })
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    public List<Player> findMatchingPlayers(
            List<Player> candidates,
            Long hostUserId,
            List<Long> excludeUserIds,
            List<Long> recentOpponentIds,
            int hostElo,
            int maxResults) {
        return findMatchingPlayers(candidates, hostUserId, excludeUserIds, recentOpponentIds,
                hostElo, null, null, null, maxResults);
    }

    public List<Player> findMatchingPlayers(
            List<Player> candidates,
            Long hostUserId,
            List<Long> excludeUserIds,
            int hostElo,
            int maxResults) {
        return findMatchingPlayers(candidates, hostUserId, excludeUserIds, Collections.emptyList(),
                hostElo, null, null, null, maxResults);
    }

    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public int[] calculateEloRange(int hostElo) {
        return calculateEloRange(hostElo, DEFAULT_ELO_RANGE);
    }

    public int[] calculateEloRange(int hostElo, int eloRange) {
        int minElo = Math.max(0, hostElo - eloRange);
        int maxElo = hostElo + eloRange;
        return new int[]{minElo, maxElo};
    }

    public boolean isEloCompatible(int playerElo, int hostElo) {
        return isEloCompatible(playerElo, hostElo, DEFAULT_ELO_RANGE);
    }

    public boolean isEloCompatible(int playerElo, int hostElo, int eloRange) {
        return Math.abs(playerElo - hostElo) <= eloRange;
    }

    public boolean isMatchFull(int currentPaidCount) {
        return currentPaidCount >= MAX_PLAYERS_PER_MATCH;
    }

    public int getPlayersNeeded() {
        return PLAYERS_NEEDED;
    }

    public int getMaxPlayersPerMatch() {
        return MAX_PLAYERS_PER_MATCH;
    }

    public int getAntiRepetitionLastN() {
        return ANTI_REPETITION_LAST_N;
    }
}
