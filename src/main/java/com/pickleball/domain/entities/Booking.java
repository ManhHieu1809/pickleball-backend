package com.pickleball.domain.entities;

import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.valueobjects.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    private Long id;
    private Long courtId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingType bookingType;
    private BookingStatus status;

    private Long createdByPlayerId;
    private Long createdByStaffId;

    private Money venueFee;
    private Money refereeFee;
    private Money platformFee;
    private Money totalCost;

    private LocalDateTime createdAt;

    private String notes;  // For walk-in booking: customer info, payment method

    @Builder.Default
    private List<BookingParticipant> participants = new ArrayList<>();



    // Business Methods
    public void confirm() {
        if (this.status == BookingStatus.PENDING) {
            this.status = BookingStatus.CONFIRMED;
        }
    }

    public void complete() {
        if (this.status == BookingStatus.CONFIRMED) {
            this.status = BookingStatus.COMPLETED;
        }
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public void addParticipant(BookingParticipant participant) {
        if (participant != null && !participants.contains(participant)) {
            participants.add(participant);
        }
    }

    public boolean isPlayerCreated() {
        return createdByPlayerId != null;
    }

    public boolean isStaffCreated() {
        return createdByStaffId != null;
    }

    public Long getCreatorId() {
        return isPlayerCreated() ? createdByPlayerId : createdByStaffId;
    }

    public void calculateCosts(Money baseVenueFee, Money refereeFee, BigDecimal platformFeePercentage) {
        this.venueFee = baseVenueFee;
        this.refereeFee = refereeFee != null ? refereeFee : new Money(BigDecimal.ZERO, "VND");

        BigDecimal platformAmount = baseVenueFee.getAmount().multiply(platformFeePercentage);
        this.platformFee = new Money(platformAmount, "VND");

        BigDecimal totalAmount = baseVenueFee.getAmount()
                .add(this.refereeFee.getAmount())
                .add(this.platformFee.getAmount());
        this.totalCost = new Money(totalAmount, "VND");
    }

    public boolean canBeCancelled() {
        return startTime.minusHours(24).isAfter(LocalDateTime.now());
    }

    public Money calculateRefundAmount() {
        if (!canBeCancelled()) {
            return new Money(BigDecimal.ZERO, "VND");
        }

        long hoursUntilStart = java.time.Duration.between(LocalDateTime.now(), startTime).toHours();
        if (hoursUntilStart > 48) {
            return totalCost; // 100% refund
        } else {
            // 50% refund
            BigDecimal refundAmount = totalCost.getAmount().multiply(new BigDecimal("0.5"));
            return new Money(refundAmount, "VND");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id) &&
                Objects.equals(courtId, booking.courtId) &&
                Objects.equals(startTime, booking.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, courtId, startTime);
    }
}