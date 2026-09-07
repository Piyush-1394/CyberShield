package com.cybershieldai.api.report;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reports;

    public ReportController(ReportService reports) {
        this.reports = reports;
    }

    @GetMapping
    public List<ReportService.ReportDto> list() {
        return reports.list();
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportService.ReportDto generate(@Valid @RequestBody ReportService.GenerateRequest req) {
        return reports.generate(req);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        byte[] bytes = reports.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + reports.filename(id))
                .contentType(MediaType.parseMediaType(reports.contentType(id)))
                .body(bytes);
    }
}
