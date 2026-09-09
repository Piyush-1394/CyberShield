package com.cybershieldai.api.organization;

import com.cybershieldai.api.common.ApiException;
import com.cybershieldai.api.common.OrgGuard;
import com.cybershieldai.api.organization.entity.OrganizationEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class OrganizationService {
    private final OrganizationRepository orgs;
    private final OrgGuard orgGuard;

    public OrganizationService(OrganizationRepository orgs, OrgGuard orgGuard) {
        this.orgs = orgs;
        this.orgGuard = orgGuard;
    }

    public OrgResponse me() {
        return toDto(orgs.findById(orgGuard.requireOrg()).orElseThrow(() -> ApiException.notFound("Organization not found")));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public OrgResponse update(@Valid OrgUpdateRequest req) {
        orgGuard.requireAdmin();
        OrganizationEntity org = orgs.findById(orgGuard.requireOrg()).orElseThrow();
        org.setName(req.name());
        org.setLogoUrl(req.logoUrl());
        if (req.budgetAvailable() != null && req.budgetAvailable().compareTo(BigDecimal.ZERO) >= 0) {
            org.setBudgetAvailable(req.budgetAvailable());
        }
        return toDto(org);
    }

    private OrgResponse toDto(OrganizationEntity org) {
        return new OrgResponse(org.getId(), org.getName(), org.getLogoUrl(), org.getBudgetAvailable(),
                org.getBudgetAllocated(), org.getCreatedAt());
    }

    public record OrgUpdateRequest(@NotBlank String name, String logoUrl, BigDecimal budgetAvailable) {}

    public record OrgResponse(Long id, String name, String logoUrl, BigDecimal budgetAvailable,
                              BigDecimal budgetAllocated, Instant createdAt) {}
}
