package com.cybershieldai.api.alert;

import com.cybershieldai.api.alert.entity.AlertEntity;
import com.cybershieldai.api.common.ApiException;
import com.cybershieldai.api.common.OrgGuard;
import com.cybershieldai.api.common.PageResponse;
import com.cybershieldai.api.common.Severity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class AlertService {
    private final AlertRepository repo;
    private final OrgGuard orgGuard;

    public AlertService(AlertRepository repo, OrgGuard orgGuard) {
        this.repo = repo;
        this.orgGuard = orgGuard;
    }

    public PageResponse<AlertDto> list(int page, int size) {
        Long orgId = orgGuard.requireOrg();
        return PageResponse.of(repo.findByOrganizationId(orgId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toDto));
    }

    public long unreadCount() {
        return repo.countByOrganizationIdAndReadFalse(orgGuard.requireOrg());
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public AlertDto markRead(Long id) {
        AlertEntity e = repo.findByIdAndOrganizationId(id, orgGuard.requireOrg())
                .orElseThrow(() -> ApiException.notFound("Alert not found"));
        e.setRead(true);
        return toDto(e);
    }

    @Transactional
    public int markAllRead() {
        return repo.markAllRead(orgGuard.requireOrg());
    }

    private AlertDto toDto(AlertEntity e) {
        return new AlertDto(e.getId(), e.getTitle(), e.getMessage(), e.getSeverity(), e.isRead(),
                e.getAsset() == null ? null : e.getAsset().getId(),
                e.getAsset() == null ? null : e.getAsset().getName(),
                e.getVulnerability() == null ? null : e.getVulnerability().getId(),
                e.getCreatedAt());
    }

    public record AlertDto(Long id, String title, String message, Severity severity, boolean read,
                           Long assetId, String assetName, Long vulnerabilityId, Instant createdAt) {}
}
