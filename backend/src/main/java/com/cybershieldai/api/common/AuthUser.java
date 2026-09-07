package com.cybershieldai.api.common;

import com.cybershieldai.api.user.entity.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public record AuthUser(Long userId, Long organizationId, Role role, String email, String fullName) {

    public static AuthUser current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthUser user)) {
            throw ApiException.unauthorized("Not authenticated");
        }
        return user;
    }

    public static AuthUser from(UserEntity user) {
        return new AuthUser(user.getId(), user.getOrganization().getId(), user.getRole(), user.getEmail(), user.getFullName());
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean canWrite() {
        return role == Role.ADMIN || role == Role.ANALYST;
    }
}
