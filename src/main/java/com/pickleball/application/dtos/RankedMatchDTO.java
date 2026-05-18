package com.pickleball.application.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RankedMatchDTO {
    private BookingDTO booking;
    private PaymentDTO payment;

    private BigDecimal depositPerPlayer;
    private String depositCurrency;
    private BigDecimal venueFee;
    private BigDecimal refereeFee;
    private BigDecimal platformFee;
    private BigDecimal totalCost;

    private Integer currentPlayerCount;
    private Integer requiredPlayerCount;
    private List<PlayerMatchDTO> teamACandidates;
    private List<PlayerMatchDTO> teamBCandidates;

    // Referee info
    private boolean refereeAssigned;
    private RefereeMatchDTO assignedReferee;
    private List<RefereeMatchDTO> refereeCandidates;

    // Ranked match status
    private Long rankedMatchId;
    private String matchStatus;             // From MatchStatus enum
}
