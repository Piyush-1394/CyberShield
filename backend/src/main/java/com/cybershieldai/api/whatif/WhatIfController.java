package com.cybershieldai.api.whatif;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/whatif")
public class WhatIfController {
    private final WhatIfService service;

    public WhatIfController(WhatIfService service) {
        this.service = service;
    }

    @GetMapping("/scenarios")
    public List<WhatIfService.ScenarioDto> scenarios() {
        return service.list();
    }

    @PostMapping("/simulate")
    public WhatIfService.SimulateResponse simulate(@Valid @RequestBody WhatIfService.SimulateRequest req) {
        return service.simulate(req);
    }
}
