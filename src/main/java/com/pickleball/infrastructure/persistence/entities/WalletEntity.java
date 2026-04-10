package com.pickleball.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "wallets")
public class WalletEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

