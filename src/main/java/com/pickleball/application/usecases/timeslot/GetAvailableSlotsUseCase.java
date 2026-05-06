package com.pickleball.application.usecases.timeslot;

import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtPricingRepository;
import com.pickleball.domain.services.PriceCalculationService;
import com.pickleball.domain.valueobjects.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class GetAvailableSlotsUseCase {

    private final BookingRepository bookingRepository;
    private final CourtPricingRepository courtPricingRepository;
    private final PriceCalculationService priceCalculationService;

    private static final LocalTime OPENING_TIME = LocalTime.of(7, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);
    private static final int SLOT_DURATION_MINUTES = 60; // Fixed 1 hour

    public static class TimeSlotInfo {
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final Money price;
        private final boolean isAvailable;

        public TimeSlotInfo(LocalTime startTime, LocalTime endTime, Money price, boolean isAvailable) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.price = price;
            this.isAvailable = isAvailable;
        }

        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public Money getPrice() { return price; }
        public boolean isAvailable() { return isAvailable; }
    }

    public List<TimeSlotInfo> execute(Long courtId, LocalDate date) {
        log.debug("Getting available slots for court {} on {}", courtId, date);

        List<CourtPricing> pricings = courtPricingRepository.findByCourtId(courtId);
        if (pricings.isEmpty()) {
            log.warn("No pricing rules for court {}", courtId);
            return new ArrayList<>();
        }

        Set<LocalTime> bookedStartTimes = bookingRepository.findByCourtIdAndDate(courtId, date)
                .stream()
                .map(booking -> booking.getStartTime().toLocalTime())
                .collect(Collectors.toSet());

        List<TimeSlotInfo> slots = new ArrayList<>();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        LocalTime currentTime = OPENING_TIME;
        while (currentTime.isBefore(CLOSING_TIME)) {
            LocalTime slotEndTime = currentTime.plusMinutes(SLOT_DURATION_MINUTES);

            Money price = priceCalculationService.calculateSlotPrice(pricings, currentTime, dayOfWeek);

            boolean isAvailable = !bookedStartTimes.contains(currentTime);

            TimeSlotInfo slot = new TimeSlotInfo(currentTime, slotEndTime, price, isAvailable);
            slots.add(slot);

            currentTime = slotEndTime;
        }

        log.debug("Generated {} slots, {} available", slots.size(),
                slots.stream().filter(TimeSlotInfo::isAvailable).count());

        return slots;
    }

    public List<TimeSlotInfo> executeAvailableOnly(Long courtId, LocalDate date) {
        return execute(courtId, date).stream()
                .filter(TimeSlotInfo::isAvailable)
                .collect(Collectors.toList());
    }
}

