package com.cybershieldai.api.common;

import org.springframework.stereotype.Component;

@Component
public class OrgGuard {

    public Long requireOrg() {
        return AuthUser.current().organizationId();
    }

    public void requireWrite() {
        if (!AuthUser.current().canWrite()) {
            throw ApiException.forbidden("Analyst or admin role required");
        }
    }

    public void requireAdmin() {
        if (!AuthUser.current().isAdmin()) {
            throw ApiException.forbidden("Admin role required");
        }
    }

    public void assertSameOrg(Long organizationId) {
        if (!AuthUser.current().organizationId().equals(organizationId)) {
            throw ApiException.forbidden("Cross-organization access denied");
        }
    }
}
