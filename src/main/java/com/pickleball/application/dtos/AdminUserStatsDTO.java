package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserStatsDTO {
    private long totalUsers;
    private long totalPlayers;
    private long totalOwners;
    private long totalReferees;
    private long totalAdmins;
    private long newUsersToday;
    private long newUsersThisMonth;
}
