package com.cybershieldai.api.billing;

import com.cybershieldai.api.billing.entity.OrganizationSubscriptionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationSubscriptionRepository extends JpaRepository<OrganizationSubscriptionEntity, Long> {
    @EntityGraph(attributePaths = "plan")
    Optional<OrganizationSubscriptionEntity> findByOrganizationId(Long organizationId);
}
