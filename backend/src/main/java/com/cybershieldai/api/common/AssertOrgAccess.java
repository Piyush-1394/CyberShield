package com.cybershieldai.api.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a service/controller method may only operate on the authenticated user's organization.
 * Combined with repositories that always filter by organizationId.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AssertOrgAccess {
    boolean write() default false;
    boolean adminOnly() default false;
}
