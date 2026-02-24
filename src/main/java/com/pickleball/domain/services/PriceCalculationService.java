package com.pickleball.domain.services;

import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.valueobjects.Money;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Domain service để tính giá slot dựa trên CourtPricing rules
 */
public class PriceCalculationService {

    /**
     * Tính giá cho một time slot dựa trên pricing rules
     *
     * @param pricings Danh sách pricing rules của court
     * @param startTime Giờ bắt đầu của slot
     * @param dayOfWeek Ngày trong tuần
     * @return Giá của slot
     */
    public Money calculateSlotPrice(List<CourtPricing> pricings, LocalTime startTime, DayOfWeek dayOfWeek) {
        // Tìm pricing rule phù hợp nhất
        // Priority: 1. Specific day + time range, 2. All days + time range

        CourtPricing specificDayPricing = findMatchingPricing(pricings, startTime, dayOfWeek.getValue());
        if (specificDayPricing != null) {
            return specificDayPricing.getPricePerHour();
        }

        // Fallback to all-days pricing (dayOfWeek = null)
        CourtPricing allDaysPricing = findMatchingPricing(pricings, startTime, null);
        if (allDaysPricing != null) {
            return allDaysPricing.getPricePerHour();
        }

        // Default fallback: return first pricing or throw exception
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
        // time >= start && time < end
        return !time.isBefore(start) && time.isBefore(end);
    }
}

