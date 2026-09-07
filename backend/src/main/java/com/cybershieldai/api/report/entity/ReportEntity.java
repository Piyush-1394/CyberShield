package com.cybershieldai.api.report.entity;

import com.cybershieldai.api.organization.entity.OrganizationEntity;
import com.cybershieldai.api.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "reports")
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OrganizationEntity organization;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;
    @Column(nullable = false)
    private String reportType;
    @Column(nullable = false)
    private String format;
    @Column(nullable = false)
    private String filePath;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
