package com.cybershieldai.api.billing;

import com.cybershieldai.api.billing.entity.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethodEntity, Long> {
    List<PaymentMethodEntity> findByOrganizationId(Long organizationId);
    Optional<PaymentMethodEntity> findByIdAndOrganizationId(Long id, Long organizationId);
}
