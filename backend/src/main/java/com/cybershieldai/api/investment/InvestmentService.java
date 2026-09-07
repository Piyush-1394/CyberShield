package com.cybershieldai.api.investment;

import com.cybershieldai.api.common.ApiException;
import com.cybershieldai.api.common.OrgGuard;
import com.cybershieldai.api.investment.entity.AppliedInvestmentEntity;
import com.cybershieldai.api.investment.entity.RemediationActionEntity;
import com.cybershieldai.api.organization.OrganizationRepository;
import com.cybershieldai.api.organization.entity.OrganizationEntity;
import com.cybershieldai.api.risk.RiskComputationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class InvestmentService {
    private final RemediationActionRepository actions;
    private final AppliedInvestmentRepository applied;
    private final OrganizationRepository orgs;
    private final OrgGuard orgGuard;
    private final RiskComputationService risk;

    public InvestmentService(RemediationActionRepository actions, AppliedInvestmentRepository applied,
                             OrganizationRepository orgs, OrgGuard orgGuard, RiskComputationService risk) {
        this.actions = actions;
        this.applied = applied;
        this.orgs = orgs;
        this.orgGuard = orgGuard;
        this.risk = risk;
    }

    public RecommendationsResponse recommendations() {
        Long orgId = orgGuard.requireOrg();
        OrganizationEntity org = orgs.findById(orgId).orElseThrow();
        BigDecimal budget = org.getBudgetAvailable();
        List<RemediationActionEntity> catalog = actions.findByOrganizationIdAndActiveTrue(orgId).stream()
                .filter(a -> !applied.existsByOrganizationIdAndActionId(orgId, a.getId()))
                .sorted(Comparator.comparing((RemediationActionEntity a) ->
                        a.getRiskReductionPercent().divide(a.getCost().max(BigDecimal.ONE), 8, RoundingMode.HALF_UP)).reversed())
                .toList();
        List<ActionDto> selected = new ArrayList<>();
        BigDecimal remaining = budget;
        BigDecimal reduction = BigDecimal.ZERO;
        for (RemediationActionEntity a : catalog) {
            if (a.getCost().compareTo(remaining) <= 0) {
                selected.add(toDto(a, true));
                remaining = remaining.subtract(a.getCost());
                reduction = reduction.add(a.getRiskReductionPercent());
            } else {
                selected.add(toDto(a, false));
            }
        }
        return new RecommendationsResponse(budget, remaining, reduction.min(BigDecimal.valueOf(70)), selected);
    }

    public ProjectionResponse projection() {
        Long orgId = orgGuard.requireOrg();
        var totals = risk.totals(orgId);
        var rec = recommendations();
        BigDecimal projected = totals.overall().multiply(
                BigDecimal.ONE.subtract(rec.expectedReductionPercent().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
        List<Point> current = new ArrayList<>();
        List<Point> projectedSeries = new ArrayList<>();
        for (int i = 0; i <= 12; i++) {
            current.add(new Point(i, totals.overall()));
            BigDecimal p = totals.overall().subtract(
                    totals.overall().subtract(projected).multiply(BigDecimal.valueOf(i / 12.0)));
            projectedSeries.add(new Point(i, p.setScale(2, RoundingMode.HALF_UP)));
        }
        return new ProjectionResponse(totals.overall(), projected.setScale(2, RoundingMode.HALF_UP), current, projectedSeries);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','ANALYST')")
    public ApplyResponse apply(ApplyRequest req) {
        orgGuard.requireWrite();
        Long orgId = orgGuard.requireOrg();
        OrganizationEntity org = orgs.findById(orgId).orElseThrow();
        BigDecimal spent = BigDecimal.ZERO;
        List<Long> ids = req.actionIds() == null ? recommendations().actions().stream()
                .filter(ActionDto::selected).map(ActionDto::id).toList() : req.actionIds();
        for (Long id : ids) {
            RemediationActionEntity action = actions.findByIdAndOrganizationId(id, orgId)
                    .orElseThrow(() -> ApiException.notFound("Action not found"));
            if (applied.existsByOrganizationIdAndActionId(orgId, id)) {
                continue;
            }
            if (action.getCost().compareTo(org.getBudgetAvailable()) > 0) {
                throw ApiException.badRequest("Insufficient budget for " + action.getName());
            }
            AppliedInvestmentEntity row = new AppliedInvestmentEntity();
            row.setOrganization(org);
            row.setAction(action);
            row.setCost(action.getCost());
            applied.save(row);
            org.setBudgetAvailable(org.getBudgetAvailable().subtract(action.getCost()));
            org.setBudgetAllocated(org.getBudgetAllocated().add(action.getCost()));
            spent = spent.add(action.getCost());
        }
        risk.recalculateOrganization(orgId);
        var totals = risk.totals(orgId);
        return new ApplyResponse(spent, org.getBudgetAvailable(), totals.overall(), totals.financialExposure());
    }

    private ActionDto toDto(RemediationActionEntity a, boolean selected) {
        return new ActionDto(a.getId(), a.getName(), a.getDescription(), a.getCost(), a.getRiskReductionPercent(),
                a.getCategory(), selected);
    }

    public record ActionDto(Long id, String name, String description, BigDecimal cost,
                            BigDecimal riskReductionPercent, String category, boolean selected) {}

    public record RecommendationsResponse(BigDecimal budgetAvailable, BigDecimal remainingBudget,
                                          BigDecimal expectedReductionPercent, List<ActionDto> actions) {}

    public record Point(int month, BigDecimal score) {}

    public record ProjectionResponse(BigDecimal currentRisk, BigDecimal projectedRisk,
                                     List<Point> currentCurve, List<Point> projectedCurve) {}

    public record ApplyRequest(List<Long> actionIds) {}

    public record ApplyResponse(BigDecimal spent, BigDecimal budgetRemaining, BigDecimal overallRisk,
                                BigDecimal financialExposure) {}
}
