package com.cybershieldai.api.alert.entity;

import com.cybershieldai.api.asset.entity.AssetEntity;
import com.cybershieldai.api.common.Severity;
import com.cybershieldai.api.organization.entity.OrganizationEntity;
import com.cybershieldai.api.vulnerability.entity.VulnerabilityEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "alerts")
public class AlertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationEntity organization;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String message;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;
    @Column(nullable = false)
    private boolean read;
    @ManyToOne(fetch = FetchType.LAZY)
    private AssetEntity asset;
    @ManyToOne(fetch = FetchType.LAZY)
    private VulnerabilityEntity vulnerability;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
