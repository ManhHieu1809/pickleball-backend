package com.pickleball.application.dtos;

import com.pickleball.domain.enums.RequestStatus;
import com.pickleball.domain.enums.RequestType;
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
public class RoleRequestDTO {
    private Long id;
    private Long userId;
    private RequestType requestType;
    private Long venueId;
    private String legalInfo;
    private BigDecimal testScore;
    private RequestStatus status;
    private LocalDateTime submittedAt;
    private Long processedByAdminId;
    private LocalDateTime processedAt;
    private String notes;
}

