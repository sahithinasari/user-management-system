package com.skinzen.user_management_system.audit;

import com.skinzen.user_management_system.dto.AuthRequest;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.UserRepository;
import com.skinzen.user_management_system.security.CustomUserPrincipal;
import com.skinzen.user_management_system.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final UserRepository userRepository;

    @AfterReturning(
            pointcut = "@annotation(auditable)"
    )
    public void auditSuccess(
            JoinPoint joinPoint,
            Auditable auditable) {

        User user = getUser(joinPoint);

        auditService.log(
                user,
                auditable.success(),
                getIpAddress()
        );
    }

    @AfterThrowing(
            pointcut = "@annotation(auditable)",
            throwing = "exception"
    )
    public void auditFailure(
            JoinPoint joinPoint,
            Auditable auditable,
            Throwable exception) {

        User user = getUser(joinPoint);

        auditService.log(
                user,
                auditable.failure(),
                getIpAddress()
        );
    }

    private User getUser(JoinPoint joinPoint) {

        /*
         * 1. For already authenticated requests,
         *    get the user from SecurityContext.
         */
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication != null &&
                authentication.isAuthenticated()) {

            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserPrincipal userPrincipal) {
                return userPrincipal.getUser();
            }
        }

        /*
         * 2. Login happens before SecurityContext is populated.
         *
         *    Therefore, try to identify the user from
         *    the AuthRequest.
         */
        for (Object argument : joinPoint.getArgs()) {

            if (argument instanceof AuthRequest request) {

                return userRepository
                        .findByEmail(request.identifier())
                        .orElse(null);
            }
        }

        /*
         * 3. Anonymous operation or user cannot be identified.
         */
        return null;
    }

    private String getIpAddress() {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        HttpServletRequest request =
                attributes.getRequest();

        return request.getRemoteAddr();
    }
}