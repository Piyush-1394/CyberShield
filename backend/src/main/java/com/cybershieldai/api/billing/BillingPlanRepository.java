package com.cybershieldai.api.billing;

import com.cybershieldai.api.billing.entity.BillingPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillingPlanRepository extends JpaRepository<BillingPlanEntity, Long> {
    Optional<BillingPlanEntity> findByCode(String code);
}
