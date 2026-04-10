package com.pickleball.application.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Ranked Match response (WORKFLOW §II.3).
 * Extends CasualMatchDTO concept with referee info and ranked-specific fields.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RankedMatchDTO {
    private BookingDTO booking;
    private PaymentDTO payment;

    // Cost breakdown
    private BigDecimal depositPerPlayer;
    private String depositCurrency;
    private BigDecimal venueFee;
    private BigDecimal refereeFee;
    private BigDecimal platformFee;
    private BigDecimal totalCost;

    // Player info
    private Integer currentPlayerCount;
    private Integer requiredPlayerCount;    // Always 4
    private List<PlayerMatchDTO> playerCandidates;

    // Referee info
    private boolean refereeAssigned;
    private RefereeMatchDTO assignedReferee;
    private List<RefereeMatchDTO> refereeCandidates;

    // Ranked match status
    private Long rankedMatchId;
    private String matchStatus;             // From MatchStatus enum
}
