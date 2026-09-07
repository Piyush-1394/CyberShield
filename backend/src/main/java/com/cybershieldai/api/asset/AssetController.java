package com.cybershieldai.api.asset;

import com.cybershieldai.api.common.PageResponse;
import com.cybershieldai.api.common.Severity;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assets;

    public AssetController(AssetService assets) {
        this.assets = assets;
    }

    @GetMapping
    public PageResponse<AssetService.AssetResponse> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Severity criticality,
            @RequestParam(required = false) BigDecimal minRisk,
            @RequestParam(required = false) BigDecimal maxRisk,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "riskScore,desc") String sort) {
        return assets.list(type, criticality, minRisk, maxRisk, page, size, sort);
    }

    @GetMapping("/top")
    public List<AssetService.AssetResponse> top(@RequestParam(defaultValue = "5") int limit) {
        return assets.topRisky(limit);
    }

    @GetMapping("/{id}")
    public AssetService.AssetResponse get(@PathVariable Long id) {
        return assets.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetService.AssetResponse create(@Valid @RequestBody AssetService.AssetRequest req) {
        return assets.create(req);
    }

    @PutMapping("/{id}")
    public AssetService.AssetResponse update(@PathVariable Long id, @Valid @RequestBody AssetService.AssetRequest req) {
        return assets.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assets.delete(id);
    }
}
