package com.cybershieldai.api.report;

import com.cybershieldai.api.report.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
    List<ReportEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    Optional<ReportEntity> findByIdAndOrganizationId(Long id, Long organizationId);
}
