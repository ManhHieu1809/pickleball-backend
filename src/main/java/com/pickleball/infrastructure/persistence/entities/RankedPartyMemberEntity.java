package com.pickleball.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ranked_party_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankedPartyMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "party_id", nullable = false)
    private Long partyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "invited_by_user_id")
    private Long invitedByUserId;

    @Column(nullable = false)
    private String status;

    @Column(name = "is_host")
    private Boolean isHost;

    private String message;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (status == null) {
            status = "INVITED";
        }
        if (isHost == null) {
            isHost = false;
        }
    }
}
