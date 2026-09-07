package com.cybershieldai.api.threatintel;

import com.cybershieldai.api.asset.AssetRepository;
import com.cybershieldai.api.common.ApiException;
import com.cybershieldai.api.common.OrgGuard;
import com.cybershieldai.api.common.PageResponse;
import com.cybershieldai.api.common.Severity;
import com.cybershieldai.api.organization.OrganizationRepository;
import com.cybershieldai.api.threatintel.entity.ThreatIntelEntity;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThreatIntelService {
    private final ThreatIntelRepository repo;
    private final AssetRepository assets;
    private final OrganizationRepository orgs;
    private final OrgGuard orgGuard;

    public ThreatIntelService(ThreatIntelRepository repo, AssetRepository assets, OrganizationRepository orgs, OrgGuard orgGuard) {
        this.repo = repo;
        this.assets = assets;
        this.orgs = orgs;
        this.orgGuard = orgGuard;
    }

    public PageResponse<ThreatResponse> list(Severity severity, LocalDate from, int page, int size) {
        Long orgId = orgGuard.requireOrg();
        Specification<ThreatIntelEntity> spec = (root, q, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("organization").get("id"), orgId));
            if (severity != null) p.add(cb.equal(root.get("severity"), severity));
            if (from != null) p.add(cb.greaterThanOrEqualTo(root.get("publishedDate"), from));
            return cb.and(p.toArray(Predicate[]::new));
        };
        return PageResponse.of(repo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedDate")))
                .map(this::toDto));
    }

    @Transactional(readOnly = true)
    public ThreatResponse get(Long id) {
        return toDto(repo.findByIdAndOrganizationId(id, orgGuard.requireOrg())
                .orElseThrow(() -> ApiException.notFound("Threat not found")));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ThreatResponse create(@Valid ThreatRequest req) {
        orgGuard.requireWrite();
        Long orgId = orgGuard.requireOrg();
        ThreatIntelEntity e = new ThreatIntelEntity();
        e.setOrganization(orgs.getReferenceById(orgId));
        apply(e, req, orgId);
        return toDto(repo.save(e));
    }

    private void apply(ThreatIntelEntity e, ThreatRequest req, Long orgId) {
        e.setTitle(req.title());
        e.setThreatActor(req.threatActor());
        e.setThreatType(req.threatType());
        e.setSeverity(req.severity());
        e.setSource(req.source());
        e.setPublishedDate(req.publishedDate());
        e.setDescription(req.description());
        e.setRelatedAssets(new HashSet<>());
        if (req.assetIds() != null) {
            for (Long aid : req.assetIds()) {
                e.getRelatedAssets().add(assets.findByIdAndOrganizationId(aid, orgId)
                        .orElseThrow(() -> ApiException.notFound("Asset not found")));
            }
        }
    }

    private ThreatResponse toDto(ThreatIntelEntity e) {
        return new ThreatResponse(e.getId(), e.getTitle(), e.getThreatActor(), e.getThreatType(), e.getSeverity(),
                e.getSource(), e.getPublishedDate(), e.getDescription(),
                e.getRelatedAssets().stream().map(a -> new RelatedAsset(a.getId(), a.getName())).toList());
    }

    public record ThreatRequest(@NotBlank String title, String threatActor, String threatType,
                                @NotNull Severity severity, String source, @NotNull LocalDate publishedDate,
                                String description, List<Long> assetIds) {}

    public record RelatedAsset(Long id, String name) {}

    public record ThreatResponse(Long id, String title, String threatActor, String threatType, Severity severity,
                                 String source, LocalDate publishedDate, String description,
                                 List<RelatedAsset> relatedAssets) {}
}
