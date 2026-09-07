package com.cybershieldai.api.threatintel;

import com.cybershieldai.api.threatintel.entity.ThreatIntelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ThreatIntelRepository extends JpaRepository<ThreatIntelEntity, Long>, JpaSpecificationExecutor<ThreatIntelEntity> {
    Optional<ThreatIntelEntity> findByIdAndOrganizationId(Long id, Long organizationId);
    List<ThreatIntelEntity> findByOrganizationId(Long organizationId);
}
