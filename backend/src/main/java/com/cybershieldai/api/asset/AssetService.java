package com.cybershieldai.api.asset;

import com.cybershieldai.api.asset.entity.AssetEntity;
import com.cybershieldai.api.common.*;
import com.cybershieldai.api.organization.OrganizationRepository;
import com.cybershieldai.api.risk.RiskComputationService;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssetService {
    private final AssetRepository repo;
    private final OrganizationRepository orgs;
    private final OrgGuard orgGuard;
    private final RiskComputationService risk;

    public AssetService(AssetRepository repo, OrganizationRepository orgs, OrgGuard orgGuard, RiskComputationService risk) {
        this.repo = repo;
        this.orgs = orgs;
        this.orgGuard = orgGuard;
        this.risk = risk;
    }

    public PageResponse<AssetResponse> list(String type, Severity criticality, BigDecimal minRisk, BigDecimal maxRisk,
                                            int page, int size, String sort) {
        Long orgId = orgGuard.requireOrg();
        Specification<AssetEntity> spec = (root, q, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("organization").get("id"), orgId));
            if (type != null && !type.isBlank()) p.add(cb.equal(root.get("type"), type));
            if (criticality != null) p.add(cb.equal(root.get("criticality"), criticality));
            if (minRisk != null) p.add(cb.ge(root.get("riskScore"), minRisk));
            if (maxRisk != null) p.add(cb.le(root.get("riskScore"), maxRisk));
            return cb.and(p.toArray(Predicate[]::new));
        };
        String[] sortParts = (sort == null ? "riskScore,desc" : sort).split(",");
        Sort.Direction dir = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(page, size, Sort.by(dir, sortParts[0]));
        return PageResponse.of(repo.findAll(spec, pageable).map(this::toDto));
    }

    public List<AssetResponse> topRisky(int limit) {
        return repo.findByOrganizationIdOrderByRiskScoreDesc(orgGuard.requireOrg()).stream()
                .limit(limit).map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AssetResponse get(Long id) {
        return toDto(load(id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    @AssertOrgAccess(write = true)
    public AssetResponse create(@Valid AssetRequest req) {
        orgGuard.requireWrite();
        AssetEntity e = new AssetEntity();
        e.setOrganization(orgs.getReferenceById(orgGuard.requireOrg()));
        apply(e, req);
        e = repo.save(e);
        risk.recalculateOrganization(orgGuard.requireOrg());
        return toDto(repo.findById(e.getId()).orElse(e));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public AssetResponse update(Long id, @Valid AssetRequest req) {
        orgGuard.requireWrite();
        AssetEntity e = load(id);
        apply(e, req);
        risk.recalculateOrganization(orgGuard.requireOrg());
        return toDto(e);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public void delete(Long id) {
        orgGuard.requireWrite();
        repo.delete(load(id));
        risk.recalculateOrganization(orgGuard.requireOrg());
    }

    private AssetEntity load(Long id) {
        return repo.findByIdAndOrganizationId(id, orgGuard.requireOrg())
                .orElseThrow(() -> ApiException.notFound("Asset not found"));
    }

    private void apply(AssetEntity e, AssetRequest req) {
        e.setName(req.name());
        e.setType(req.type());
        e.setCategory(req.category());
        e.setCriticality(req.criticality());
        e.setFinancialExposure(req.financialExposure());
        e.setOwner(req.owner());
        e.setStatus(req.status() == null ? "ACTIVE" : req.status());
        e.setLastScannedAt(req.lastScannedAt());
    }

    private AssetResponse toDto(AssetEntity e) {
        return new AssetResponse(e.getId(), e.getName(), e.getType(), e.getCategory(), e.getCriticality(),
                e.getRiskScore(), e.getFinancialExposure(), e.getOwner(), e.getLastScannedAt(), e.getStatus());
    }

    public record AssetRequest(@NotBlank String name, @NotBlank String type, @NotBlank String category,
                               @NotNull Severity criticality, @NotNull BigDecimal financialExposure,
                               String owner, String status, Instant lastScannedAt) {}

    public record AssetResponse(Long id, String name, String type, String category, Severity criticality,
                                BigDecimal riskScore, BigDecimal financialExposure, String owner,
                                Instant lastScannedAt, String status) {}
}
