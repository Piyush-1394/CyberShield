package com.cybershieldai.api.billing.entity;

import com.cybershieldai.api.organization.entity.OrganizationEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "organization_subscriptions")
public class OrganizationSubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationEntity organization;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private BillingPlanEntity plan;
    @Column(nullable = false)
    private LocalDate renewalDate;
}
