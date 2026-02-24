package com.pickleball.domain.entities;

import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.valueobjects.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingParticipant {
    private Long id;
    private Long bookingId;
    private Long userId;
    private ParticipantRole role;
    private String team;
    private JoinStatus joinStatus;

    private Money depositAmount;
    private Money actualPaymentAmount;
    private Money refundAmount;
    private boolean isMatchHost;



    public void markAsPaid() {
        this.joinStatus = JoinStatus.PAID;
    }

    public void markAsCheckedIn() {
        this.joinStatus = JoinStatus.CHECKED_IN;
    }

    public void markAsForfeited() {
        this.joinStatus = JoinStatus.FORFEITED;
    }

    public void setDepositAmount(Money amount) {
        this.depositAmount = amount;
    }

    public void setTeam(String team) {
        if (team != null && (team.equals("A") || team.equals("B"))) {
            this.team = team;
        }
    }

    public boolean hasPaid() {
        return joinStatus == JoinStatus.PAID || joinStatus == JoinStatus.CHECKED_IN;
    }

    public boolean isCheckedIn() {
        return joinStatus == JoinStatus.CHECKED_IN;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingParticipant that = (BookingParticipant) o;
        return Objects.equals(bookingId, that.bookingId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, userId);
    }
}