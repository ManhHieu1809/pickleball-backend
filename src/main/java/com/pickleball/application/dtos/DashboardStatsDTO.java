package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsDTO {

    // Summary cards
    private long totalBookings;
    private long todayBookings;
    private long newUsersThisMonth;
    private long activeVenues;

    // Pending actions
    private long pendingOwnerRequests;
    private long pendingVenues;

    // Booking type distribution
    private Map<String, Long> bookingTypeDistribution;

    // Recent bookings
    private List<RecentBookingDTO> recentBookings;

    // Pending role requests
    private List<PendingActionDTO> pendingActions;

    @Data
    @Builder
    public static class RecentBookingDTO {
        private Long id;
        private Long courtId;
        private String courtName;
        private String bookingType;
        private String status;
        private BigDecimal totalCost;
        private String startTime;
    }

    @Data
    @Builder
    public static class PendingActionDTO {
        private Long id;
        private String type; // OWNER_REQUEST, VENUE_PENDING
        private String title;
        private String description;
        private String submittedAt;
    }
}
