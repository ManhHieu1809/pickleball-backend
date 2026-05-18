package com.pickleball.application.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.ParticipantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingParticipantDTO {
    private Long id;
    private Long userId;
    private ParticipantRole role;
    private String team;
    private JoinStatus joinStatus;
    private boolean matchHost;

    private BigDecimal depositAmount;
    private BigDecimal actualPaymentAmount;
    private BigDecimal refundAmount;
}
