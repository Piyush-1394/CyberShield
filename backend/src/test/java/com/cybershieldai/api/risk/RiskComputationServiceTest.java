package com.cybershieldai.api.risk;

import com.cybershieldai.api.asset.AssetRepository;
import com.cybershieldai.api.asset.entity.AssetEntity;
import com.cybershieldai.api.common.Severity;
import com.cybershieldai.api.investment.AppliedInvestmentRepository;
import com.cybershieldai.api.organization.OrganizationRepository;
import com.cybershieldai.api.threatintel.ThreatIntelRepository;
import com.cybershieldai.api.vulnerability.VulnerabilityRepository;
import com.cybershieldai.api.vulnerability.entity.VulnerabilityEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskComputationServiceTest {
    @Mock AssetRepository assets;
    @Mock VulnerabilityRepository vulns;
    @Mock ThreatIntelRepository threats;
    @Mock AppliedInvestmentRepository applied;
    @Mock RiskSnapshotRepository snapshots;
    @Mock OrganizationRepository orgs;
    @InjectMocks RiskComputationService service;

    @Test
    void overallIsExposureWeightedAverage() {
        AssetEntity a1 = asset(1L, Severity.CRITICAL, "80", "1000");
        AssetEntity a2 = asset(2L, Severity.LOW, "20", "1000");
        when(assets.findByOrganizationId(1L)).thenReturn(List.of(a1, a2));
        var totals = service.totals(1L);
        assertThat(totals.overall()).isEqualByComparingTo("50.00");
        assertThat(totals.financialExposure()).isEqualByComparingTo("1000.00");
    }

    @Test
    void patchedVulnsDoNotRaiseScore() {
        AssetEntity asset = asset(9L, Severity.MEDIUM, "0", "100");
        VulnerabilityEntity open = new VulnerabilityEntity();
        open.setAsset(asset);
        open.setSeverity(Severity.CRITICAL);
        open.setStatus(VulnerabilityEntity.Status.PATCHED);
        BigDecimal score = service.scoreAsset(asset, List.of(open), List.of(), 1.0);
        assertThat(score).isEqualByComparingTo("11.25");
    }

    private AssetEntity asset(Long id, Severity crit, String score, String exp) {
        AssetEntity a = new AssetEntity();
        a.setId(id);
        a.setCriticality(crit);
        a.setRiskScore(new BigDecimal(score));
        a.setFinancialExposure(new BigDecimal(exp));
        a.setCategory("Applications");
        return a;
    }
}
