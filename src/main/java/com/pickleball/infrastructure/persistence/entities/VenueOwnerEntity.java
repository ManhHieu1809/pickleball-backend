package com.pickleball.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "venue_owners")
public class VenueOwnerEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Column(name = "bank_account_number", length = 100)
    private String bankAccountNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;
}

