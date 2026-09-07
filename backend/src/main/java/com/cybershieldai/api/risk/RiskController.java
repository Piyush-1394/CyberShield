package com.cybershieldai.api.risk;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/risk")
public class RiskController {
    private final RiskService risk;

    public RiskController(RiskService risk) {
        this.risk = risk;
    }

    @GetMapping("/overview")
    public RiskService.OverviewResponse overview() {
        return risk.overview();
    }

    @GetMapping("/distribution")
    public RiskService.DistributionResponse distribution() {
        return risk.distribution();
    }

    @GetMapping("/trend")
    public RiskService.TrendResponse trend(@RequestParam(defaultValue = "30") int days) {
        return risk.trend(days);
    }

    @GetMapping("/ai-summary")
    public RiskService.AiSummaryResponse aiSummary() {
        return risk.aiSummary();
    }
}
