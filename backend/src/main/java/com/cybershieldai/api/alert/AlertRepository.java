package com.cybershieldai.api.alert;

import com.cybershieldai.api.alert.entity.AlertEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {
    Page<AlertEntity> findByOrganizationId(Long organizationId, Pageable pageable);
    Optional<AlertEntity> findByIdAndOrganizationId(Long id, Long organizationId);
    long countByOrganizationIdAndReadFalse(Long organizationId);

    @Modifying
    @Query("update AlertEntity a set a.read = true where a.organization.id = :orgId")
    int markAllRead(Long orgId);
}
