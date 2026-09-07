package com.cybershieldai.api.settings;

import com.cybershieldai.api.auth.dto.AuthDtos.UserResponse;
import com.cybershieldai.api.common.ApiException;
import com.cybershieldai.api.common.AuthUser;
import com.cybershieldai.api.organization.OrganizationService;
import com.cybershieldai.api.settings.entity.NotificationPreferenceEntity;
import com.cybershieldai.api.user.UserRepository;
import com.cybershieldai.api.user.entity.UserEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {
    private final UserRepository users;
    private final NotificationPreferenceRepository prefs;
    private final PasswordEncoder encoder;
    private final OrganizationService organizations;

    public SettingsService(UserRepository users, NotificationPreferenceRepository prefs, PasswordEncoder encoder,
                           OrganizationService organizations) {
        this.users = users;
        this.prefs = prefs;
        this.encoder = encoder;
        this.organizations = organizations;
    }

    public OrganizationService.OrgResponse org() {
        return organizations.me();
    }

    public OrganizationService.OrgResponse updateOrg(@Valid OrganizationService.OrgUpdateRequest req) {
        return organizations.update(req);
    }

    public PrefDto notifications() {
        NotificationPreferenceEntity p = prefs.findById(AuthUser.current().userId())
                .orElseGet(this::createDefault);
        return new PrefDto(p.isEmailAlerts(), p.isInAppAlerts(), p.isWeeklyDigest());
    }

    @Transactional
    public PrefDto updateNotifications(@Valid PrefDto req) {
        NotificationPreferenceEntity p = prefs.findById(AuthUser.current().userId()).orElseGet(this::createDefault);
        p.setEmailAlerts(req.emailAlerts());
        p.setInAppAlerts(req.inAppAlerts());
        p.setWeeklyDigest(req.weeklyDigest());
        return notifications();
    }

    @Transactional
    public UserResponse updateProfile(@Valid ProfileRequest req) {
        UserEntity user = users.findById(AuthUser.current().userId()).orElseThrow();
        if (!user.getEmail().equalsIgnoreCase(req.email()) && users.existsByEmailIgnoreCase(req.email())) {
            throw ApiException.conflict("Email already registered");
        }
        user.setFullName(req.fullName());
        user.setEmail(req.email().toLowerCase());
        user.setAvatarUrl(req.avatarUrl());
        if (req.password() != null && !req.password().isBlank()) {
            if (req.password().length() < 10) {
                throw ApiException.badRequest("Password must be at least 10 characters");
            }
            user.setPasswordHash(encoder.encode(req.password()));
        }
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(),
                user.getAvatarUrl(), user.getOrganization().getId(), user.getOrganization().getName());
    }

    private NotificationPreferenceEntity createDefault() {
        UserEntity user = users.findById(AuthUser.current().userId()).orElseThrow();
        NotificationPreferenceEntity p = new NotificationPreferenceEntity();
        p.setUser(user);
        return prefs.save(p);
    }

    public record PrefDto(boolean emailAlerts, boolean inAppAlerts, boolean weeklyDigest) {}

    public record ProfileRequest(@NotBlank String fullName, @Email @NotBlank String email, String avatarUrl,
                                 String password) {}
}
