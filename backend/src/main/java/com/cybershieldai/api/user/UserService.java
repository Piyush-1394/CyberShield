package com.cybershieldai.api.user;

import com.cybershieldai.api.auth.AuthService;
import com.cybershieldai.api.auth.dto.AuthDtos.UserResponse;
import com.cybershieldai.api.common.ApiException;
import com.cybershieldai.api.common.AuthUser;
import com.cybershieldai.api.common.OrgGuard;
import com.cybershieldai.api.common.Role;
import com.cybershieldai.api.user.entity.UserEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository users;
    private final OrgGuard orgGuard;
    private final AuthService auth;

    public UserService(UserRepository users, OrgGuard orgGuard, AuthService auth) {
        this.users = users;
        this.orgGuard = orgGuard;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return users.findByOrganizationId(orgGuard.requireOrg()).stream().map(AuthService::toUser).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public InviteResponse invite(@Valid InviteRequest req) {
        orgGuard.requireAdmin();
        String token = auth.createInvite(req.email(), req.role());
        return new InviteResponse(req.email(), req.role(), token);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse changeRole(Long id, @Valid RoleRequest req) {
        orgGuard.requireAdmin();
        UserEntity user = load(id);
        if (user.getId().equals(AuthUser.current().userId()) && req.role() != Role.ADMIN) {
            throw ApiException.badRequest("Cannot demote yourself");
        }
        user.setRole(req.role());
        return AuthService.toUser(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void remove(Long id) {
        orgGuard.requireAdmin();
        UserEntity user = load(id);
        if (user.getId().equals(AuthUser.current().userId())) {
            throw ApiException.badRequest("Cannot remove yourself");
        }
        if (user.getRole() == Role.ADMIN && users.findByOrganizationId(orgGuard.requireOrg()).stream()
                .filter(u -> u.getRole() == Role.ADMIN).count() <= 1) {
            throw ApiException.badRequest("Cannot remove the last admin");
        }
        users.delete(user);
    }

    private UserEntity load(Long id) {
        return users.findByIdAndOrganizationId(id, orgGuard.requireOrg())
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    public record InviteRequest(@Email @NotBlank String email, @NotNull Role role) {}

    public record RoleRequest(@NotNull Role role) {}

    public record InviteResponse(String email, Role role, String inviteToken) {}
}
