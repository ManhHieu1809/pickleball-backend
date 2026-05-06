package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalBookings;
    private long todayBookings;
    private long newUsersThisMonth;
    private long activeVenues;

    private long pendingOwnerRequests;
    private long pendingVenues;

    private Map<String, Long> bookingTypeDistribution;

    private List<BigDecimal> revenueLast7Days;

    private List<RecentBookingDTO> recentBookings;

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
        private String type;
        private String title;
        private String description;
        private String submittedAt;
    }
}
