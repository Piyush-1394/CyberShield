package com.cybershieldai.api.common;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OrgAccessAspect {

    @Before("@annotation(assertOrgAccess) || @within(assertOrgAccess)")
    public void check(JoinPoint joinPoint, AssertOrgAccess assertOrgAccess) {
        AuthUser user = AuthUser.current();
        if (assertOrgAccess.adminOnly() && !user.isAdmin()) {
            throw ApiException.forbidden("Admin role required");
        }
        if (assertOrgAccess.write() && !user.canWrite()) {
            throw ApiException.forbidden("Read-only role cannot modify data");
        }
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long orgHint && "organizationId".equals(paramNameGuess(joinPoint, arg))) {
                if (!orgHint.equals(user.organizationId())) {
                    throw ApiException.forbidden("Cross-organization access denied");
                }
            }
        }
    }

    private String paramNameGuess(JoinPoint joinPoint, Object arg) {
        return "";
    }
}
