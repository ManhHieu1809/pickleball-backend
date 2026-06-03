package com.pickleball.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ranked_parties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankedPartyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_user_id", nullable = false)
    private Long hostUserId;

    @Column(nullable = false)
    private String status;

    private Double latitude;
    private Double longitude;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "queued_at")
    private LocalDateTime queuedAt;

    @Column(name = "requested_start_time")
    private LocalDateTime requestedStartTime;

    @Column(name = "requested_end_time")
    private LocalDateTime requestedEndTime;

    @Column(name = "matched_booking_id")
    private Long matchedBookingId;

    @PrePersist
    void onCreate() {
        if (status == null) {
            status = "OPEN";
        }
    }
}
