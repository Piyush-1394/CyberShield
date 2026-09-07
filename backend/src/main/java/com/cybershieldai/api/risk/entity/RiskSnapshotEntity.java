package com.cybershieldai.api.risk.entity;

import com.cybershieldai.api.organization.entity.OrganizationEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "risk_snapshots")
public class RiskSnapshotEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationEntity organization;
    @Column(nullable = false)
    private LocalDate snapshotDate;
    @Column(nullable = false)
    private BigDecimal overallScore;
    @Column(nullable = false)
    private BigDecimal financialExposure;
}
