package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminBookingStatsDTO {
    private long totalBookings;
    private long todayBookings;
    private long activeBookings; // CONFIRMED OR PENDING
    private long cancelledBookings;
}
