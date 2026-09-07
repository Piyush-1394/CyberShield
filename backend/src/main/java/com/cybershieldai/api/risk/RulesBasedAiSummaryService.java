package com.cybershieldai.api.risk;

import com.cybershieldai.api.asset.AssetRepository;
import com.cybershieldai.api.asset.entity.AssetEntity;
import com.cybershieldai.api.common.Severity;
import com.cybershieldai.api.vulnerability.VulnerabilityRepository;
import com.cybershieldai.api.vulnerability.entity.VulnerabilityEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RulesBasedAiSummaryService implements AiSummaryService {
    private final VulnerabilityRepository vulns;
    private final AssetRepository assets;
    private final RiskComputationService computation;

    public RulesBasedAiSummaryService(VulnerabilityRepository vulns, AssetRepository assets,
                                      RiskComputationService computation) {
        this.vulns = vulns;
        this.assets = assets;
        this.computation = computation;
    }

    @Override
    public RiskService.AiSummaryResponse summarize(Long organizationId) {
        List<VulnerabilityEntity> open = vulns.findByOrganizationId(organizationId).stream()
                .filter(v -> v.getStatus() == VulnerabilityEntity.Status.OPEN
                        || v.getStatus() == VulnerabilityEntity.Status.IN_PROGRESS)
                .toList();
        long critical = open.stream().filter(v -> v.getSeverity() == Severity.CRITICAL).count();
        List<AssetEntity> risky = assets.findByOrganizationIdOrderByRiskScoreDesc(organizationId);
        String assetNames = risky.stream().limit(3).map(AssetEntity::getName).collect(Collectors.joining(", "));
        var totals = computation.totals(organizationId);
        long recent = open.stream().filter(v -> v.getDiscoveredDate().isAfter(LocalDate.now().minusDays(30))).count();
        String summary = String.format(
                "Overall risk is %.1f/100 with ₹%s financial exposure. %d new issues in 30 days, including %d critical vulnerabilities. Highest concentration sits on %s. Recommend applying the top investment actions while budget remains ₹ available at org level.",
                totals.overall(), totals.financialExposure().toPlainString(), recent, critical,
                assetNames.isBlank() ? "key systems" : assetNames);
        String tone = totals.overall().compareTo(BigDecimal.valueOf(70)) >= 0 ? "elevated" : "stable";
        return new RiskService.AiSummaryResponse(summary, tone);
    }
}
