package com.cybershieldai.api.auth;

import com.cybershieldai.api.auth.entity.InviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<InviteEntity, Long> {
    Optional<InviteEntity> findByTokenHash(String tokenHash);
    List<InviteEntity> findByOrganizationIdAndUsedFalse(Long organizationId);
}
