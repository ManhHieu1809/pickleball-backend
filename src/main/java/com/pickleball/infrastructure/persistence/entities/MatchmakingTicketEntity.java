package com.pickleball.infrastructure.persistence.entities;

import com.pickleball.domain.enums.ParticipantRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matchmaking_queue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchmakingTicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    private Double latitude;
    private Double longitude;
    private Integer elo;

    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) joinedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }
}

