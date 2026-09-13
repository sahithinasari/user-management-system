package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.enums.AuditEvent;
import com.skinzen.user_management_system.model.AuditLog;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(
            User user,
            AuditEvent event,
            String ipAddress,
            String metadata) {

        AuditLog auditLog = new AuditLog(
                user,
                event,
                ipAddress,
                metadata
        );

        auditLogRepository.save(auditLog);
    }

    public void log(
            User user,
            AuditEvent event,
            String ipAddress) {

        log(user, event, ipAddress, null);
    }
}