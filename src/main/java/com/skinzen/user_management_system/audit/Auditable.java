package com.skinzen.user_management_system.audit;

import com.skinzen.user_management_system.enums.AuditEvent;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    AuditEvent success();

    AuditEvent failure() default AuditEvent.LOGIN_FAILED;
}