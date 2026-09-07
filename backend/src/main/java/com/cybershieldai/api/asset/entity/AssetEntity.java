package com.cybershieldai.api.asset.entity;

import com.cybershieldai.api.common.Severity;
import com.cybershieldai.api.organization.entity.OrganizationEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "assets")
public class AssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationEntity organization;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String type;
    @Column(nullable = false)
    private String category;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity criticality;
    @Column(nullable = false)
    private BigDecimal riskScore = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal financialExposure = BigDecimal.ZERO;
    private String owner;
    private Instant lastScannedAt;
    @Column(nullable = false)
    private String status = "ACTIVE";
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
