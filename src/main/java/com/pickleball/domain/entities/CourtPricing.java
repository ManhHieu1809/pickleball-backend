package com.pickleball.domain.entities;

import com.pickleball.domain.valueobjects.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourtPricing {
    private Long id;
    private Long courtId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Money pricePerHour;



    public boolean appliesToDay(DayOfWeek checkDay) {
        return dayOfWeek == null || dayOfWeek.equals(checkDay);
    }

    public boolean isTimeWithinRange(LocalTime checkTime) {
        return !checkTime.isBefore(startTime) && checkTime.isBefore(endTime);
    }

    public Money calculateCost(long hours) {
        BigDecimal totalAmount = pricePerHour.getAmount().multiply(BigDecimal.valueOf(hours));
        return new Money(totalAmount, pricePerHour.getCurrency());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CourtPricing that = (CourtPricing) o;
        return Objects.equals(courtId, that.courtId) &&
                Objects.equals(dayOfWeek, that.dayOfWeek) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courtId, dayOfWeek, startTime, endTime);
    }
}