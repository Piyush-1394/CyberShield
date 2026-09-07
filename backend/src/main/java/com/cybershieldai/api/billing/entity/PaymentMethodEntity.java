package com.cybershieldai.api.billing.entity;

import com.cybershieldai.api.organization.entity.OrganizationEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payment_methods")
public class PaymentMethodEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationEntity organization;
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    private String last4;
    @Column(nullable = false)
    private String methodType;
    private String gatewayToken;
    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
