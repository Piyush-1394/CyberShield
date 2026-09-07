package com.cybershieldai.api.risk;

import com.cybershieldai.api.asset.AssetRepository;
import com.cybershieldai.api.asset.entity.AssetEntity;
import com.cybershieldai.api.common.Severity;
import com.cybershieldai.api.investment.AppliedInvestmentRepository;
import com.cybershieldai.api.investment.entity.AppliedInvestmentEntity;
import com.cybershieldai.api.organization.OrganizationRepository;
import com.cybershieldai.api.risk.entity.RiskSnapshotEntity;
import com.cybershieldai.api.threatintel.ThreatIntelRepository;
import com.cybershieldai.api.threatintel.entity.ThreatIntelEntity;
import com.cybershieldai.api.vulnerability.VulnerabilityRepository;
import com.cybershieldai.api.vulnerability.entity.VulnerabilityEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Asset risk = clamp(0–100,
 *   0.45 * vulnScore + 0.30 * threatScore + 0.25 * criticalityWeight)
 * then multiplied by (1 - appliedReduction), where appliedReduction is the
 * compounded residual of applied investment actions (capped at 0.70).
 *
 * vulnScore: max weighted open/in-progress vuln (CRITICAL=100, HIGH=75, MEDIUM=45, LOW=20).
 * threatScore: max related threat intel severity using the same scale.
 * criticalityWeight: asset criticality on the same scale.
 * Org overall = exposure-weighted average of asset risk scores.
 * Financial exposure = sum(asset.financialExposure * riskScore / 100).
 */
@Service
public class RiskComputationService {
    private final AssetRepository assets;
    private final VulnerabilityRepository vulns;
    private final ThreatIntelRepository threats;
    private final AppliedInvestmentRepository applied;
    private final RiskSnapshotRepository snapshots;
    private final OrganizationRepository orgs;

    public RiskComputationService(AssetRepository assets, VulnerabilityRepository vulns, ThreatIntelRepository threats,
                                  AppliedInvestmentRepository applied, RiskSnapshotRepository snapshots,
                                  OrganizationRepository orgs) {
        this.assets = assets;
        this.vulns = vulns;
        this.threats = threats;
        this.applied = applied;
        this.snapshots = snapshots;
        this.orgs = orgs;
    }

    public static BigDecimal severityWeight(Severity s) {
        return switch (s) {
            case CRITICAL -> BigDecimal.valueOf(100);
            case HIGH -> BigDecimal.valueOf(75);
            case MEDIUM -> BigDecimal.valueOf(45);
            case LOW -> BigDecimal.valueOf(20);
        };
    }

    @Transactional
    public void recalculateOrganization(Long orgId) {
        List<AssetEntity> list = assets.findByOrganizationId(orgId);
        List<VulnerabilityEntity> allVulns = vulns.findByOrganizationId(orgId);
        List<ThreatIntelEntity> allThreats = threats.findByOrganizationId(orgId);
        double residual = residualMultiplier(orgId);
        for (AssetEntity asset : list) {
            asset.setRiskScore(scoreAsset(asset, allVulns, allThreats, residual));
        }
        persistSnapshot(orgId);
    }

    public BigDecimal scoreAsset(AssetEntity asset, List<VulnerabilityEntity> allVulns,
                                 List<ThreatIntelEntity> allThreats, double residual) {
        BigDecimal vulnScore = allVulns.stream()
                .filter(v -> v.getAsset().getId().equals(asset.getId()))
                .filter(v -> v.getStatus() != VulnerabilityEntity.Status.PATCHED
                        && v.getStatus() != VulnerabilityEntity.Status.ACCEPTED_RISK)
                .map(v -> severityWeight(v.getSeverity()))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal threatScore = allThreats.stream()
                .filter(t -> t.getRelatedAssets().stream().anyMatch(a -> a.getId().equals(asset.getId())))
                .map(t -> severityWeight(t.getSeverity()))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal crit = severityWeight(asset.getCriticality());
        BigDecimal raw = vulnScore.multiply(BigDecimal.valueOf(0.45))
                .add(threatScore.multiply(BigDecimal.valueOf(0.30)))
                .add(crit.multiply(BigDecimal.valueOf(0.25)));
        BigDecimal clamped = raw.min(BigDecimal.valueOf(100)).max(BigDecimal.ZERO);
        return clamped.multiply(BigDecimal.valueOf(residual)).setScale(2, RoundingMode.HALF_UP);
    }

    public double residualMultiplier(Long orgId) {
        double reduction = 0;
        for (AppliedInvestmentEntity row : applied.findByOrganizationId(orgId)) {
            reduction += row.getAction().getRiskReductionPercent().doubleValue() / 100.0;
        }
        reduction = Math.min(0.70, reduction);
        return 1.0 - reduction;
    }

    public OrgRiskTotals totals(Long orgId) {
        List<AssetEntity> list = assets.findByOrganizationId(orgId);
        BigDecimal exposureWeighted = BigDecimal.ZERO;
        BigDecimal exposureSum = BigDecimal.ZERO;
        BigDecimal financial = BigDecimal.ZERO;
        int criticalAtRisk = 0;
        Severity top = Severity.LOW;
        Map<String, BigDecimal> byCat = new java.util.LinkedHashMap<>();
        Map<String, Integer> catCount = new java.util.HashMap<>();
        for (AssetEntity a : list) {
            BigDecimal score = a.getRiskScore() == null ? BigDecimal.ZERO : a.getRiskScore();
            BigDecimal exp = a.getFinancialExposure() == null ? BigDecimal.ZERO : a.getFinancialExposure();
            exposureWeighted = exposureWeighted.add(score.multiply(exp));
            exposureSum = exposureSum.add(exp.max(BigDecimal.ONE));
            financial = financial.add(exp.multiply(score).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            if (a.getCriticality() == Severity.CRITICAL && score.compareTo(BigDecimal.valueOf(60)) >= 0) {
                criticalAtRisk++;
            }
            if (score.compareTo(severityWeight(top)) > 0) {
                if (score.doubleValue() >= 85) top = Severity.CRITICAL;
                else if (score.doubleValue() >= 70) top = Severity.HIGH;
                else if (score.doubleValue() >= 40) top = Severity.MEDIUM;
            }
            byCat.merge(a.getCategory(), score, BigDecimal::add);
            catCount.merge(a.getCategory(), 1, Integer::sum);
        }
        BigDecimal overall = exposureSum.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : exposureWeighted.divide(exposureSum, 2, RoundingMode.HALF_UP);
        Map<String, BigDecimal> dist = new java.util.LinkedHashMap<>();
        byCat.forEach((k, v) -> dist.put(k, v.divide(BigDecimal.valueOf(catCount.get(k)), 2, RoundingMode.HALF_UP)));
        return new OrgRiskTotals(overall, financial, criticalAtRisk, top, dist);
    }

    @Transactional
    public void persistSnapshot(Long orgId) {
        OrgRiskTotals t = totals(orgId);
        LocalDate today = LocalDate.now();
        RiskSnapshotEntity snap = snapshots.findByOrganizationIdAndSnapshotDate(orgId, today)
                .orElseGet(RiskSnapshotEntity::new);
        snap.setOrganization(orgs.getReferenceById(orgId));
        snap.setSnapshotDate(today);
        snap.setOverallScore(t.overall());
        snap.setFinancialExposure(t.financialExposure());
        snapshots.save(snap);
    }

    public record OrgRiskTotals(BigDecimal overall, BigDecimal financialExposure, int criticalAssetsAtRisk,
                                Severity topThreatLevel, Map<String, BigDecimal> categoryAverages) {}
}
