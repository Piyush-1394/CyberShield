package com.cybershieldai.api.organization.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "organizations")
public class OrganizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String logoUrl;
    @Column(nullable = false)
    private BigDecimal budgetAvailable = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal budgetAllocated = BigDecimal.ZERO;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
