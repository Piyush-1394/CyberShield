package com.cybershieldai.api.risk;

import com.cybershieldai.api.common.AuthUser;
import com.cybershieldai.api.common.OrgGuard;
import com.cybershieldai.api.common.Severity;
import com.cybershieldai.api.risk.entity.RiskSnapshotEntity;
import com.cybershieldai.api.vulnerability.VulnerabilityRepository;
import com.cybershieldai.api.vulnerability.entity.VulnerabilityEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RiskService {
    private final RiskComputationService computation;
    private final RiskSnapshotRepository snapshots;
    private final VulnerabilityRepository vulns;
    private final OrgGuard orgGuard;
    private final AiSummaryService aiSummary;

    public RiskService(RiskComputationService computation, RiskSnapshotRepository snapshots,
                       VulnerabilityRepository vulns, OrgGuard orgGuard, AiSummaryService aiSummary) {
        this.computation = computation;
        this.snapshots = snapshots;
        this.vulns = vulns;
        this.orgGuard = orgGuard;
        this.aiSummary = aiSummary;
    }

    @PreAuthorize("isAuthenticated()")
    public OverviewResponse overview() {
        Long orgId = orgGuard.requireOrg();
        var t = computation.totals(orgId);
        List<Point> trend = trend(30).series();
        BigDecimal delta = BigDecimal.ZERO;
        if (trend.size() >= 2) {
            delta = trend.get(trend.size() - 1).score().subtract(trend.get(0).score());
        }
        return new OverviewResponse(t.overall(), t.financialExposure(), t.criticalAssetsAtRisk(),
                t.topThreatLevel(), delta, trend);
    }

    @PreAuthorize("isAuthenticated()")
    public DistributionResponse distribution() {
        Long orgId = orgGuard.requireOrg();
        Map<String, BigDecimal> avg = computation.totals(orgId).categoryAverages();
        List<Slice> slices = avg.entrySet().stream()
                .map(e -> new Slice(e.getKey(), e.getValue()))
                .toList();
        return new DistributionResponse(slices);
    }

    @PreAuthorize("isAuthenticated()")
    public TrendResponse trend(int days) {
        Long orgId = orgGuard.requireOrg();
        int d = Math.max(7, Math.min(days, 365));
        LocalDate from = LocalDate.now().minusDays(d);
        List<RiskSnapshotEntity> rows = snapshots
                .findByOrganizationIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(orgId, from);
        if (rows.isEmpty()) {
            computation.recalculateOrganization(orgId);
            rows = snapshots.findByOrganizationIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(orgId, from);
        }
        List<Point> series = new ArrayList<>();
        for (RiskSnapshotEntity r : rows) {
            series.add(new Point(r.getSnapshotDate().toString(), r.getOverallScore(), r.getFinancialExposure()));
        }
        return new TrendResponse(series);
    }

    @PreAuthorize("isAuthenticated()")
    public AiSummaryResponse aiSummary() {
        AuthUser user = AuthUser.current();
        return aiSummary.summarize(user.organizationId());
    }

    public record OverviewResponse(BigDecimal overallRiskScore, BigDecimal financialExposure,
                                   int criticalAssetsAtRisk, Severity topThreatLevel,
                                   BigDecimal thirtyDayDelta, List<Point> trend) {}

    public record DistributionResponse(List<Slice> categories) {}

    public record Slice(String name, BigDecimal score) {}

    public record TrendResponse(List<Point> series) {}

    public record Point(String date, BigDecimal score, BigDecimal financialExposure) {}

    public record AiSummaryResponse(String summary, String tone) {}
}
