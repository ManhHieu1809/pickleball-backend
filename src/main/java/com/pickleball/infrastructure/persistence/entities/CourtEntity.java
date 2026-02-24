package com.pickleball.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "courts")
public class CourtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venue_id", nullable = false)
    private Long venueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", insertable = false, updatable = false)
    private VenueEntity venue;

    @Column(name = "court_name")
    private String courtName;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "deactivated_by_admin_id")
    private Long deactivatedByAdminId;


    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourtPricingEntity> pricing = new ArrayList<>();

}