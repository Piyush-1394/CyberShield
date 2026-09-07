package com.cybershieldai.api.auth;

import com.cybershieldai.api.auth.dto.AuthDtos.*;
import com.cybershieldai.api.auth.entity.InviteEntity;
import com.cybershieldai.api.auth.entity.PasswordResetTokenEntity;
import com.cybershieldai.api.auth.entity.RefreshTokenEntity;
import com.cybershieldai.api.billing.BillingPlanRepository;
import com.cybershieldai.api.billing.OrganizationSubscriptionRepository;
import com.cybershieldai.api.billing.entity.OrganizationSubscriptionEntity;
import com.cybershieldai.api.common.ApiException;
import com.cybershieldai.api.common.AuthUser;
import com.cybershieldai.api.common.Role;
import com.cybershieldai.api.config.AppProperties;
import com.cybershieldai.api.organization.OrganizationRepository;
import com.cybershieldai.api.organization.entity.OrganizationEntity;
import com.cybershieldai.api.settings.NotificationPreferenceRepository;
import com.cybershieldai.api.settings.entity.NotificationPreferenceEntity;
import com.cybershieldai.api.user.UserRepository;
import com.cybershieldai.api.user.entity.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {
    private final UserRepository users;
    private final OrganizationRepository orgs;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordResetTokenRepository resets;
    private final InviteRepository invites;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final TokenHasher hasher;
    private final EmailService email;
    private final AppProperties props;
    private final BillingPlanRepository plans;
    private final OrganizationSubscriptionRepository subscriptions;
    private final NotificationPreferenceRepository prefs;

    public AuthService(UserRepository users, OrganizationRepository orgs, RefreshTokenRepository refreshTokens,
                       PasswordResetTokenRepository resets, InviteRepository invites, PasswordEncoder encoder,
                       JwtService jwt, TokenHasher hasher, EmailService email, AppProperties props,
                       BillingPlanRepository plans, OrganizationSubscriptionRepository subscriptions,
                       NotificationPreferenceRepository prefs) {
        this.users = users;
        this.orgs = orgs;
        this.refreshTokens = refreshTokens;
        this.resets = resets;
        this.invites = invites;
        this.encoder = encoder;
        this.jwt = jwt;
        this.hasher = hasher;
        this.email = email;
        this.props = props;
        this.plans = plans;
        this.subscriptions = subscriptions;
        this.prefs = prefs;
    }

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        if (users.existsByEmailIgnoreCase(req.email())) {
            throw ApiException.conflict("Email already registered");
        }
        OrganizationEntity org;
        Role role;
        if (req.inviteToken() != null && !req.inviteToken().isBlank()) {
            InviteEntity invite = invites.findByTokenHash(hasher.hash(req.inviteToken()))
                    .orElseThrow(() -> ApiException.badRequest("Invalid invite token"));
            if (invite.isUsed() || invite.getExpiresAt().isBefore(Instant.now())) {
                throw ApiException.badRequest("Invite expired");
            }
            if (!invite.getEmail().equalsIgnoreCase(req.email())) {
                throw ApiException.badRequest("Invite email does not match");
            }
            invite.setUsed(true);
            org = invite.getOrganization();
            role = invite.getRole();
        } else {
            if (req.organizationName() == null || req.organizationName().isBlank()) {
                throw ApiException.badRequest("Organization name is required");
            }
            OrganizationEntity newOrg = new OrganizationEntity();
            newOrg.setName(req.organizationName());
            newOrg.setBudgetAvailable(new BigDecimal("5000000"));
            newOrg.setBudgetAllocated(BigDecimal.ZERO);
            final OrganizationEntity savedOrg = orgs.save(newOrg);
            org = savedOrg;
            plans.findByCode("GROWTH").ifPresent(plan -> {
                OrganizationSubscriptionEntity sub = new OrganizationSubscriptionEntity();
                sub.setOrganization(savedOrg);
                sub.setPlan(plan);
                sub.setRenewalDate(LocalDate.now().plusYears(1));
                subscriptions.save(sub);
            });
            role = Role.ADMIN;
        }
        UserEntity user = new UserEntity();
        user.setOrganization(org);
        user.setEmail(req.email().toLowerCase());
        user.setFullName(req.fullName());
        user.setPasswordHash(encoder.encode(req.password()));
        user.setRole(role);
        user = users.save(user);
        NotificationPreferenceEntity pref = new NotificationPreferenceEntity();
        pref.setUser(user);
        prefs.save(pref);
        return issue(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest req) {
        UserEntity user = users.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!user.isEnabled() || !encoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return issue(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw ApiException.unauthorized("Missing refresh token");
        }
        RefreshTokenEntity stored = refreshTokens.findByTokenHash(hasher.hash(refreshToken))
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.unauthorized("Refresh token expired");
        }
        if (!stored.getUser().isEnabled()) {
            throw ApiException.unauthorized("Account is disabled");
        }
        stored.setRevoked(true);
        return issue(stored.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokens.findByTokenHash(hasher.hash(refreshToken)).ifPresent(t -> t.setRevoked(true));
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        UserEntity user = users.findById(AuthUser.current().userId()).orElseThrow();
        return toUser(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        UserEntity user = users.findById(AuthUser.current().userId()).orElseThrow();
        if (!encoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Current password is incorrect");
        }
        user.setPasswordHash(encoder.encode(req.newPassword()));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        users.findByEmailIgnoreCase(req.email()).ifPresent(user -> {
            String raw = hasher.randomToken();
            PasswordResetTokenEntity token = new PasswordResetTokenEntity();
            token.setUser(user);
            token.setTokenHash(hasher.hash(raw));
            token.setExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS));
            resets.save(token);
            email.send(user.getEmail(), "Reset your CyberShield AI password",
                    "Use this reset token: " + raw);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        PasswordResetTokenEntity token = resets.findByTokenHash(hasher.hash(req.token()))
                .orElseThrow(() -> ApiException.badRequest("Invalid reset token"));
        if (token.isUsed() || token.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.badRequest("Reset token expired");
        }
        token.setUsed(true);
        token.getUser().setPasswordHash(encoder.encode(req.newPassword()));
    }

    @Transactional
    public String createInvite(String emailAddr, Role role) {
        AuthUser actor = AuthUser.current();
        if (!actor.isAdmin()) {
            throw ApiException.forbidden("Admin role required");
        }
        String raw = hasher.randomToken();
        InviteEntity invite = new InviteEntity();
        invite.setOrganization(orgs.getReferenceById(actor.organizationId()));
        invite.setEmail(emailAddr.toLowerCase());
        invite.setRole(role);
        invite.setTokenHash(hasher.hash(raw));
        invite.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invites.save(invite);
        this.email.send(emailAddr, "You are invited to CyberShield AI", "Invite token: " + raw);
        return raw;
    }

    private TokenResponse issue(UserEntity user) {
        String refreshRaw = hasher.randomToken();
        RefreshTokenEntity rt = new RefreshTokenEntity();
        rt.setUser(user);
        rt.setTokenHash(hasher.hash(refreshRaw));
        rt.setExpiresAt(Instant.now().plus(props.jwt().refreshTokenDays(), ChronoUnit.DAYS));
        refreshTokens.save(rt);
        AuthUser principal = AuthUser.from(user);
        return new TokenResponse(jwt.createAccessToken(principal), refreshRaw, toUser(user));
    }

    public static UserResponse toUser(UserEntity user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(),
                user.getAvatarUrl(), user.getOrganization().getId(), user.getOrganization().getName());
    }
}
