package com.pickleball.domain.services;

import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.valueobjects.Money;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class PriceCalculationService {

    public Money calculateSlotPrice(List<CourtPricing> pricings, LocalTime startTime, DayOfWeek dayOfWeek) {
        CourtPricing specificDayPricing = findMatchingPricing(pricings, startTime, dayOfWeek.getValue());
        if (specificDayPricing != null) {
            return specificDayPricing.getPricePerHour();
        }

        CourtPricing allDaysPricing = findMatchingPricing(pricings, startTime, null);
        if (allDaysPricing != null) {
            return allDaysPricing.getPricePerHour();
        }

        if (!pricings.isEmpty()) {
            return pricings.get(0).getPricePerHour();
        }

        throw new IllegalStateException("Không tìm thấy pricing rule phù hợp cho slot này");
    }

    private CourtPricing findMatchingPricing(List<CourtPricing> pricings, LocalTime startTime, Integer dayOfWeek) {
        return pricings.stream()
                .filter(p -> matchesDayOfWeek(p, dayOfWeek))
                .filter(p -> isTimeInRange(startTime, p.getStartTime(), p.getEndTime()))
                .findFirst()
                .orElse(null);
    }

    private boolean matchesDayOfWeek(CourtPricing pricing, Integer dayOfWeek) {
        Integer pricingDay = pricing.getDayOfWeek() != null ? pricing.getDayOfWeek().getValue() : null;
        return pricingDay == null || pricingDay.equals(dayOfWeek);
    }

    private boolean isTimeInRange(LocalTime time, LocalTime start, LocalTime end) {
        return !time.isBefore(start) && time.isBefore(end);
    }
}

