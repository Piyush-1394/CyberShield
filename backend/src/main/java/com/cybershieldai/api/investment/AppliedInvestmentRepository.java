package com.cybershieldai.api.investment;

import com.cybershieldai.api.investment.entity.AppliedInvestmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppliedInvestmentRepository extends JpaRepository<AppliedInvestmentEntity, Long> {
    @Query("select a from AppliedInvestmentEntity a join fetch a.action where a.organization.id = :organizationId")
    List<AppliedInvestmentEntity> findByOrganizationId(Long organizationId);
    boolean existsByOrganizationIdAndActionId(Long organizationId, Long actionId);
}
