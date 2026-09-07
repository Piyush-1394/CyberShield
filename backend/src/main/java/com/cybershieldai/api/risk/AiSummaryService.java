package com.cybershieldai.api.risk;

public interface AiSummaryService {
    RiskService.AiSummaryResponse summarize(Long organizationId);
}
