package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.enums.AuditEvent;
import com.skinzen.user_management_system.model.AuditLog;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void log_shouldSaveAuditRecord() {

        User user = new User();

        auditService.log(
                user,
                AuditEvent.LOGIN_SUCCESS,
                "127.0.0.1"
        );

        verify(auditLogRepository)
                .save(any(AuditLog.class));
    }

}