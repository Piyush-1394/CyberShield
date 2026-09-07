package com.cybershieldai.api.settings;

import com.cybershieldai.api.auth.dto.AuthDtos.UserResponse;
import com.cybershieldai.api.organization.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final SettingsService settings;

    public SettingsController(SettingsService settings) {
        this.settings = settings;
    }

    @GetMapping("/organization")
    public OrganizationService.OrgResponse org() {
        return settings.org();
    }

    @PutMapping("/organization")
    public OrganizationService.OrgResponse updateOrg(@Valid @RequestBody OrganizationService.OrgUpdateRequest req) {
        return settings.updateOrg(req);
    }

    @GetMapping("/notifications")
    public SettingsService.PrefDto notifications() {
        return settings.notifications();
    }

    @PutMapping("/notifications")
    public SettingsService.PrefDto updateNotifications(@Valid @RequestBody SettingsService.PrefDto req) {
        return settings.updateNotifications(req);
    }

    @PutMapping("/profile")
    public UserResponse profile(@Valid @RequestBody SettingsService.ProfileRequest req) {
        return settings.updateProfile(req);
    }
}
