package com.cybershieldai.api.investment;

import com.cybershieldai.api.investment.entity.RemediationActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RemediationActionRepository extends JpaRepository<RemediationActionEntity, Long> {
    List<RemediationActionEntity> findByOrganizationIdAndActiveTrue(Long organizationId);
    Optional<RemediationActionEntity> findByIdAndOrganizationId(Long id, Long organizationId);
}
