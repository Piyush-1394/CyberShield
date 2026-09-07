package com.cybershieldai.api.investment.entity;

import com.cybershieldai.api.organization.entity.OrganizationEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "applied_investments")
public class AppliedInvestmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationEntity organization;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private RemediationActionEntity action;
    @Column(nullable = false)
    private BigDecimal cost;
    @Column(nullable = false)
    private Instant appliedAt = Instant.now();
}
