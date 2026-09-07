package com.cybershieldai.api.organization;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public OrganizationService.OrgResponse me() {
        return service.me();
    }

    @PutMapping("/me")
    public OrganizationService.OrgResponse update(@Valid @RequestBody OrganizationService.OrgUpdateRequest req) {
        return service.update(req);
    }
}
