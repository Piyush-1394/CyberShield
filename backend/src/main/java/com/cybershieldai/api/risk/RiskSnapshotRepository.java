package com.cybershieldai.api.risk;

import com.cybershieldai.api.risk.entity.RiskSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RiskSnapshotRepository extends JpaRepository<RiskSnapshotEntity, Long> {
    List<RiskSnapshotEntity> findByOrganizationIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(Long organizationId, LocalDate from);
    java.util.Optional<RiskSnapshotEntity> findByOrganizationIdAndSnapshotDate(Long organizationId, LocalDate date);
}
