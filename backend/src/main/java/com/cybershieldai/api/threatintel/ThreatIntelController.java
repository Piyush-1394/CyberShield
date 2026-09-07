package com.cybershieldai.api.threatintel;

import com.cybershieldai.api.common.PageResponse;
import com.cybershieldai.api.common.Severity;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/threat-intel")
public class ThreatIntelController {
    private final ThreatIntelService service;

    public ThreatIntelController(ThreatIntelService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ThreatIntelService.ThreatResponse> list(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(severity, from, page, size);
    }

    @GetMapping("/{id}")
    public ThreatIntelService.ThreatResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThreatIntelService.ThreatResponse create(@Valid @RequestBody ThreatIntelService.ThreatRequest req) {
        return service.create(req);
    }
}
