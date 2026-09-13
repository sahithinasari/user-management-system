package com.skinzen.user_management_system.model;

import com.skinzen.user_management_system.enums.AuditEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditEvent event;

    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 1000)
    private String metadata;

    public AuditLog(
            User user,
            AuditEvent event,
            String ipAddress,
            String metadata) {

        this.user = user;
        this.event = event;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
        this.metadata = metadata;
    }
}