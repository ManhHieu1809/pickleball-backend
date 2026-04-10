package com.pickleball.domain.entities;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Wallet {
    private Long userId;
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    private LocalDateTime updatedAt;

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }
}

