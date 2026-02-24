package com.pickleball.infrastructure.persistence.entities;

import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.ParticipantRole;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "booking_participants")
public class BookingParticipantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private BookingEntity booking;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    @Column(name = "team")
    private String team; // 'A' or 'B'

    @Enumerated(EnumType.STRING)
    @Column(name = "join_status", nullable = false)
    private JoinStatus joinStatus;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "actual_payment_amount", precision = 10, scale = 2)
    private BigDecimal actualPaymentAmount;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "is_match_host")
    private Boolean isMatchHost;

}