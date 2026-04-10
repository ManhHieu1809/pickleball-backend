package com.pickleball.domain.entities;

import com.pickleball.domain.enums.CheckInMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckIn {
    private Long id;
    private Long bookingId;
    private Long userId;
    private CheckInMethod checkInMethod;
    private LocalDateTime checkInTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
