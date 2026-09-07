package com.cybershieldai.api.investment;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investment")
public class InvestmentController {
    private final InvestmentService service;

    public InvestmentController(InvestmentService service) {
        this.service = service;
    }

    @GetMapping("/recommendations")
    public InvestmentService.RecommendationsResponse recommendations() {
        return service.recommendations();
    }

    @GetMapping("/projection")
    public InvestmentService.ProjectionResponse projection() {
        return service.projection();
    }

    @PostMapping("/apply")
    public InvestmentService.ApplyResponse apply(@RequestBody(required = false) InvestmentService.ApplyRequest req) {
        return service.apply(req == null ? new InvestmentService.ApplyRequest(null) : req);
    }
}
