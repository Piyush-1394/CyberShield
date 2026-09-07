package com.cybershieldai.api.alert;

import com.cybershieldai.api.common.PageResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AlertService.AlertDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "limit", required = false) Integer limit) {
        int s = limit != null ? limit : size;
        return service.list(page, s);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread() {
        return Map.of("count", service.unreadCount());
    }

    @PostMapping("/{id}/read")
    public AlertService.AlertDto read(@PathVariable Long id) {
        return service.markRead(id);
    }

    @PostMapping("/read-all")
    public Map<String, Integer> readAll() {
        return Map.of("updated", service.markAllRead());
    }
}
