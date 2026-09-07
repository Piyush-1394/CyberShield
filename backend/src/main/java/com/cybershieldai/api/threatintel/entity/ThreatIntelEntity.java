package com.cybershieldai.api.threatintel.entity;

import com.cybershieldai.api.asset.entity.AssetEntity;
import com.cybershieldai.api.common.Severity;
import com.cybershieldai.api.organization.entity.OrganizationEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "threat_intel")
public class ThreatIntelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationEntity organization;
    @Column(nullable = false)
    private String title;
    private String threatActor;
    private String threatType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;
    private String source;
    @Column(nullable = false)
    private LocalDate publishedDate;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToMany
    @JoinTable(name = "threat_intel_assets",
            joinColumns = @JoinColumn(name = "threat_id"),
            inverseJoinColumns = @JoinColumn(name = "asset_id"))
    private Set<AssetEntity> relatedAssets = new HashSet<>();
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
