package com.cybershieldai.api.asset;

import com.cybershieldai.api.asset.entity.AssetEntity;
import com.cybershieldai.api.common.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<AssetEntity, Long>, JpaSpecificationExecutor<AssetEntity> {
    Optional<AssetEntity> findByIdAndOrganizationId(Long id, Long organizationId);
    List<AssetEntity> findByOrganizationIdOrderByRiskScoreDesc(Long organizationId);
    List<AssetEntity> findByOrganizationId(Long organizationId);
    long countByOrganizationIdAndCriticalityAndRiskScoreGreaterThan(Long organizationId, Severity criticality, java.math.BigDecimal score);
}
