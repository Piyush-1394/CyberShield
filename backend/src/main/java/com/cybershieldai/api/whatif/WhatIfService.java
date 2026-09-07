package com.cybershieldai.api.whatif;

import com.cybershieldai.api.common.ApiException;
import com.cybershieldai.api.common.OrgGuard;
import com.cybershieldai.api.investment.RemediationActionRepository;
import com.cybershieldai.api.investment.entity.RemediationActionEntity;
import com.cybershieldai.api.risk.RiskComputationService;
import com.cybershieldai.api.whatif.entity.WhatIfScenarioEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class WhatIfService {
    private final WhatIfScenarioRepository scenarios;
    private final RemediationActionRepository actions;
    private final OrgGuard orgGuard;
    private final RiskComputationService risk;

    public WhatIfService(WhatIfScenarioRepository scenarios, RemediationActionRepository actions,
                         OrgGuard orgGuard, RiskComputationService risk) {
        this.scenarios = scenarios;
        this.actions = actions;
        this.orgGuard = orgGuard;
        this.risk = risk;
    }

    public List<ScenarioDto> list() {
        return scenarios.findByOrganizationId(orgGuard.requireOrg()).stream()
                .map(s -> new ScenarioDto(s.getId(), s.getName(), s.getDescription(),
                        s.getControl() == null ? null : s.getControl().getId(), s.getDelayDays()))
                .toList();
    }

    public SimulateResponse simulate(@Valid SimulateRequest req) {
        Long orgId = orgGuard.requireOrg();
        Long controlId = req.controlId();
        Integer delayDays = req.delayDays();
        if (controlId == null && req.scenarioId() != null) {
            Long scenarioId = req.scenarioId();
            WhatIfScenarioEntity sc = scenarios.findByOrganizationId(orgId).stream()
                    .filter(s -> s.getId().equals(scenarioId))
                    .findFirst().orElseThrow(() -> ApiException.notFound("Scenario not found"));
            controlId = sc.getControl() == null ? null : sc.getControl().getId();
            if (delayDays == null) {
                delayDays = sc.getDelayDays();
            }
        }
        var totals = risk.totals(orgId);
        BigDecimal current = totals.financialExposure();
        BigDecimal reduction = BigDecimal.ZERO;
        if (controlId != null) {
            RemediationActionEntity action = actions.findByIdAndOrganizationId(controlId, orgId)
                    .orElseThrow(() -> ApiException.notFound("Control not found"));
            reduction = action.getRiskReductionPercent();
        }
        BigDecimal withControl = current.multiply(BigDecimal.ONE.subtract(
                reduction.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
        int delay = delayDays == null ? 30 : delayDays;
        BigDecimal delayFactor = BigDecimal.valueOf(Math.min(0.35, delay / 365.0 * 0.8));
        BigDecimal delayed = current.multiply(BigDecimal.ONE.add(delayFactor));
        BigDecimal additional = delayed.subtract(withControl);
        BigDecimal pct = withControl.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : additional.multiply(BigDecimal.valueOf(100)).divide(current.max(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        return new SimulateResponse(current, withControl.setScale(2, RoundingMode.HALF_UP),
                delayed.setScale(2, RoundingMode.HALF_UP), additional.setScale(2, RoundingMode.HALF_UP), pct);
    }

    public record ScenarioDto(Long id, String name, String description, Long controlId, int delayDays) {}

    public record SimulateRequest(Long scenarioId, Long controlId, Integer delayDays) {}

    public record SimulateResponse(BigDecimal currentExposure, BigDecimal withControlExposure,
                                   BigDecimal delayedExposure, BigDecimal additionalRisk, BigDecimal percentIncrease) {}
}
