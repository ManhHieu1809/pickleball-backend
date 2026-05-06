package com.pickleball.application.services;

import com.pickleball.application.dtos.AdminFinanceStatsDTO;
import com.pickleball.application.dtos.AdminTransactionDTO;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import com.pickleball.infrastructure.persistence.entities.TransactionEntity;
import com.pickleball.infrastructure.persistence.entities.UserEntity;
import com.pickleball.infrastructure.persistence.repositories.BookingJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.TransactionJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.UserJpaRepository;
import com.pickleball.presentation.responses.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceManagementService {
    
    private final BookingJpaRepository bookingJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;
    private final UserJpaRepository userJpaRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminFinanceStatsDTO getFinanceStats() {
        List<BookingEntity> bookings = bookingJpaRepository.findAll();
        
        BigDecimal gmv = BigDecimal.ZERO;
        BigDecimal refunds = BigDecimal.ZERO;
        long successfulCount = 0;

        for (BookingEntity b : bookings) {
            if (b.getStatus() == com.pickleball.domain.enums.BookingStatus.CONFIRMED || b.getStatus() == com.pickleball.domain.enums.BookingStatus.COMPLETED) {
                gmv = gmv.add(b.getTotalCost() != null ? b.getTotalCost() : BigDecimal.ZERO);
                successfulCount++;
            } else if (b.getStatus() == com.pickleball.domain.enums.BookingStatus.CANCELLED) {
                refunds = refunds.add(b.getTotalCost() != null ? b.getTotalCost() : BigDecimal.ZERO);
            }
        }

        BigDecimal platformRevenue = gmv.multiply(new BigDecimal("0.05"));

        return AdminFinanceStatsDTO.builder()
                .totalGMV(gmv)
                .totalRevenue(platformRevenue)
                .totalRefunds(refunds)
                .successfulBookingsCount(successfulCount)
                .build();
    }

    public com.pickleball.application.dtos.FinanceChartDTO getFinanceChart(String period) {
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        LocalDate today = LocalDate.now();

        if ("WEEK".equalsIgnoreCase(period)) {
            for (int i = 6; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                labels.add(d.getDayOfMonth() + "/" + d.getMonthValue());
                data.add(calculateRevenue(d.atStartOfDay(), d.plusDays(1).atStartOfDay()));
            }
        } else if ("MONTH".equalsIgnoreCase(period)) {
            for (int i = 29; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                labels.add(d.getDayOfMonth() + "/" + d.getMonthValue());
                data.add(calculateRevenue(d.atStartOfDay(), d.plusDays(1).atStartOfDay()));
            }
        } else if ("YEAR".equalsIgnoreCase(period)) {
            YearMonth currentMonth = YearMonth.now();
            for (int i = 11; i >= 0; i--) {
                YearMonth ym = currentMonth.minusMonths(i);
                labels.add(ym.getMonthValue() + "/" + ym.getYear());
                LocalDateTime start = ym.atDay(1).atStartOfDay();
                LocalDateTime end = ym.atEndOfMonth().plusDays(1).atStartOfDay();
                data.add(calculateRevenue(start, end));
            }
        }

        return com.pickleball.application.dtos.FinanceChartDTO.builder()
                .labels(labels)
                .data(data)
                .build();
    }

    private BigDecimal calculateRevenue(LocalDateTime start, LocalDateTime end) {
        List<BookingEntity> dailyBookings = bookingJpaRepository.findByStartTimeBetween(start, end);
        return dailyBookings.stream()
                .filter(b -> b.getStatus() == com.pickleball.domain.enums.BookingStatus.CONFIRMED || b.getStatus() == com.pickleball.domain.enums.BookingStatus.COMPLETED)
                .map(b -> b.getTotalCost() != null ? b.getTotalCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public PaginatedResponse<AdminTransactionDTO> getAllTransactions(int page, int size, String search, String type, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionEntity> tPage = transactionJpaRepository.searchTransactions(search, type, status, pageable);

        List<AdminTransactionDTO> dtos = tPage.getContent().stream()
                .map(this::mapToTransactionDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(dtos, tPage.getNumber(), tPage.getSize(), tPage.getTotalElements());
    }

    private AdminTransactionDTO mapToTransactionDto(TransactionEntity t) {
            String userName = "Unknown";
            if (t.getUserId() != null) {
                UserEntity user = userJpaRepository.findById(t.getUserId()).orElse(null);
                if (user != null) userName = user.getFullName() != null ? user.getFullName() : user.getEmail();
            }

            return AdminTransactionDTO.builder()
                    .id(t.getId())
                    .userId(t.getUserId())
                    .userName(userName)
                    .bookingId(t.getBookingId())
                    .amount(t.getAmount())
                    .type(t.getType() != null ? t.getType() : "N/A")
                    .status(t.getStatus() != null ? t.getStatus() : "N/A")
                    .description(t.getDescription())
                    .createdAt(t.getCreatedAt() != null ? t.getCreatedAt().format(DATE_FMT) : "")
                    .build();
    }
}
