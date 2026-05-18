package com.pickleball.application.usecases.player;

import com.pickleball.application.dtos.PlayerRankedStatsDTO;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GetPlayerRankedStatsUseCase {

    private final BookingRepository bookingRepository;
    private final RankedMatchRepository rankedMatchRepository;

    public PlayerRankedStatsDTO execute(Long userId) {
        List<Booking> userBookings = bookingRepository.findByParticipantUserId(userId);

        int totalRanked = 0;
        int wins = 0;
        int losses = 0;

        for (Booking booking : userBookings) {
            if (booking.getBookingType() == BookingType.RANKED) {
                // Find user team
                String userTeam = null;
                if (booking.getParticipants() != null) {
                    for (BookingParticipant p : booking.getParticipants()) {
                        if (p.getUserId().equals(userId)) {
                            userTeam = p.getTeam();
                            break;
                        }
                    }
                }

                if (userTeam != null) {
                    // Find ranked match
                    RankedMatch rankedMatch = rankedMatchRepository.findByBookingId(booking.getId()).orElse(null);
                    if (rankedMatch != null && rankedMatch.getWinningTeam() != null) {
                        totalRanked++;
                        if (rankedMatch.getWinningTeam().equals(userTeam)) {
                            wins++;
                        } else {
                            losses++;
                        }
                    }
                }
            }
        }

        double winRate = 0.0;
        double lossRate = 0.0;
        if (totalRanked > 0) {
            winRate = ((double) wins / totalRanked) * 100.0;
            lossRate = ((double) losses / totalRanked) * 100.0;
        }

        return PlayerRankedStatsDTO.builder()
                .totalRankedMatches(totalRanked)
                .wins(wins)
                .losses(losses)
                .winRate(Math.round(winRate * 100.0) / 100.0)
                .lossRate(Math.round(lossRate * 100.0) / 100.0)
                .build();
    }
}
