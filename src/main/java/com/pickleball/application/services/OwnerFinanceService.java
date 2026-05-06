package com.pickleball.application.services;

import com.pickleball.application.dtos.finance.CourtPerformanceDTO;
import com.pickleball.application.dtos.finance.OwnerFinanceOverviewDTO;
import com.pickleball.application.dtos.finance.TopCustomerDTO;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import com.pickleball.infrastructure.persistence.entities.CourtEntity;
import com.pickleball.infrastructure.persistence.entities.UserEntity;
import com.pickleball.infrastructure.persistence.repositories.BookingJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.CourtJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerFinanceService {
    private final BookingJpaRepository bookingRepository;
    private final UserJpaRepository userRepository;
    private final CourtJpaRepository courtRepository;

    public OwnerFinanceOverviewDTO getOverview(Long ownerId, String period) {
        List<BookingEntity> bookings = bookingRepository.findByOwnerId(ownerId);
        
        List<BookingEntity> validBookings = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.toList());

        BigDecimal grossRevenue = BigDecimal.ZERO;
        BigDecimal platformFee = BigDecimal.ZERO;
        
        Map<Long, CourtStats> courtStatsMap = new HashMap<>();
        Map<Long, CustomerStats> customerStatsMap = new HashMap<>();

        for (BookingEntity b : validBookings) {
            BigDecimal cost = b.getTotalCost() != null ? b.getTotalCost() : BigDecimal.ZERO;
            BigDecimal fee = b.getPlatformFee() != null ? b.getPlatformFee() : cost.multiply(new BigDecimal("0.2"));
            
            grossRevenue = grossRevenue.add(cost);
            platformFee = platformFee.add(fee);

            Long courtId = b.getCourtId();
            CourtStats cStats = courtStatsMap.computeIfAbsent(courtId, k -> new CourtStats(courtId));
            cStats.addBooking(cost);

            Long customerId = b.getCreatedByPlayerId();
            if (customerId != null) {
                CustomerStats custStats = customerStatsMap.computeIfAbsent(customerId, k -> new CustomerStats(customerId));
                custStats.addBooking(cost);
            }
        }

        BigDecimal netRevenue = grossRevenue.subtract(platformFee);

        List<CourtPerformanceDTO> courtPerformances = courtStatsMap.values().stream().map(c -> {
            String courtName = courtRepository.findById(c.courtId).map(CourtEntity::getCourtName).orElse("Unknown Court");
            // Tạm tính dummy occupancy rate (chỉ để demo)
            double occupancy = Math.min(100.0, c.totalBookings * 5.0); 
            return CourtPerformanceDTO.builder()
                    .courtId(c.courtId)
                    .courtName(courtName)
                    .totalBookings(c.totalBookings)
                    .revenue(c.revenue)
                    .occupancyRate(occupancy)
                    .build();
        }).collect(Collectors.toList());

        List<TopCustomerDTO> topCustomers = customerStatsMap.values().stream()
                .sorted((a, b) -> b.revenue.compareTo(a.revenue))
                .limit(5)
                .map(c -> {
                    String name = userRepository.findById(c.userId).map(UserEntity::getFullName).orElse("Unknown User");
                    return TopCustomerDTO.builder()
                            .userId(c.userId)
                            .userName(name)
                            .totalBookings(c.totalBookings)
                            .totalSpent(c.revenue)
                            .build();
                }).collect(Collectors.toList());

        return OwnerFinanceOverviewDTO.builder()
                .grossRevenue(grossRevenue)
                .platformFee(platformFee)
                .netRevenue(netRevenue)
                .totalBookings((long) validBookings.size())
                .courtPerformances(courtPerformances)
                .topCustomers(topCustomers)
                .build();
    }

    private static class CourtStats {
        Long courtId;
        int totalBookings;
        BigDecimal revenue = BigDecimal.ZERO;
        CourtStats(Long courtId) { this.courtId = courtId; }
        void addBooking(BigDecimal cost) {
            this.totalBookings++;
            this.revenue = this.revenue.add(cost);
        }
    }

    private static class CustomerStats {
        Long userId;
        int totalBookings;
        BigDecimal revenue = BigDecimal.ZERO;
        CustomerStats(Long userId) { this.userId = userId; }
        void addBooking(BigDecimal cost) {
            this.totalBookings++;
            this.revenue = this.revenue.add(cost);
        }
    }
}
