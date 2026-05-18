package com.pickleball.application.usecases.player;

import com.pickleball.application.dtos.PlayerWeeklyStatsDTO;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GetPlayerWeeklyStatsUseCase {

    private final BookingRepository bookingRepository;

    public PlayerWeeklyStatsDTO execute(Long userId) {
        List<Booking> allUserBookings = bookingRepository.findByParticipantUserId(userId);

        LocalDateTime now = LocalDateTime.now();

        // This week: Monday to Sunday
        LocalDateTime startOfThisWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfThisWeek = startOfThisWeek.plusDays(7).minusNanos(1);

        // Last week
        LocalDateTime startOfLastWeek = startOfThisWeek.minusDays(7);
        LocalDateTime endOfLastWeek = startOfThisWeek.minusNanos(1);

        List<Booking> thisWeekBookings = allUserBookings.stream()
                .filter(b -> b.getStartTime() != null && !b.getStartTime().isBefore(startOfThisWeek) && !b.getStartTime().isAfter(endOfThisWeek))
                .collect(Collectors.toList());

        List<Booking> lastWeekBookings = allUserBookings.stream()
                .filter(b -> b.getStartTime() != null && !b.getStartTime().isBefore(startOfLastWeek) && !b.getStartTime().isAfter(endOfLastWeek))
                .collect(Collectors.toList());

        int thisWeekCount = thisWeekBookings.size();
        int lastWeekCount = lastWeekBookings.size();

        double percentageChange = 0.0;
        if (lastWeekCount == 0 && thisWeekCount > 0) {
            percentageChange = 100.0;
        } else if (lastWeekCount > 0) {
            percentageChange = ((double) (thisWeekCount - lastWeekCount) / lastWeekCount) * 100.0;
        }

        Map<String, Integer> matchesPerDay = new LinkedHashMap<>();
        matchesPerDay.put("Monday", 0);
        matchesPerDay.put("Tuesday", 0);
        matchesPerDay.put("Wednesday", 0);
        matchesPerDay.put("Thursday", 0);
        matchesPerDay.put("Friday", 0);
        matchesPerDay.put("Saturday", 0);
        matchesPerDay.put("Sunday", 0);

        for (Booking b : thisWeekBookings) {
            String day = b.getStartTime().getDayOfWeek().name();
            // Capitalize first letter
            day = day.substring(0, 1).toUpperCase() + day.substring(1).toLowerCase();
            matchesPerDay.put(day, matchesPerDay.getOrDefault(day, 0) + 1);
        }

        return PlayerWeeklyStatsDTO.builder()
                .totalMatchesThisWeek(thisWeekCount)
                .percentageChange(Math.round(percentageChange * 100.0) / 100.0)
                .matchesPerDay(matchesPerDay)
                .build();
    }
}
