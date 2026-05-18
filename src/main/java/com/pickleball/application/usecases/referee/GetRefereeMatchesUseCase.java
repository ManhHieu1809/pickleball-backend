package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetRefereeMatchesUseCase {

    private final RankedMatchRepository rankedMatchRepository;
    private final BookingRepository bookingRepository;

    public List<RefereeMatchInfo> execute(Long refereeId, String status, LocalDate date) {
        List<RankedMatch> matches = rankedMatchRepository.findByRefereeId(refereeId);

        if (status != null && !status.isEmpty()) {
            if (status.equalsIgnoreCase("UPCOMING")) {
                matches = matches.stream().filter(m ->
                        m.getStatus() == MatchStatus.PENDING ||
                        m.getStatus() == MatchStatus.SUBMITTED
                ).collect(Collectors.toList());
            } else if (status.equalsIgnoreCase("HISTORY")) {
                matches = matches.stream().filter(m ->
                        m.getStatus() == MatchStatus.CONFIRMED ||
                        m.getStatus() == MatchStatus.RESOLVED ||
                        m.getStatus() == MatchStatus.CANCELLED ||
                        m.getStatus() == MatchStatus.IN_DISPUTE
                ).collect(Collectors.toList());
            }
        }

        return matches.stream()
            .map(match -> {
                Booking booking = bookingRepository.findById(match.getBookingId()).orElse(null);
                return new RefereeMatchInfo(match, booking);
            })
            .filter(info -> {
                if (date == null || info.booking() == null) {
                    return true;
                }
                return info.booking().getStartTime().toLocalDate().equals(date);
            })
            .collect(Collectors.toList());
    }

    public record RefereeMatchInfo(RankedMatch match, Booking booking) {
    }
}
