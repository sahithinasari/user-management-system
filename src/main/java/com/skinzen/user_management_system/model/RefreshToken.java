package com.skinzen.user_management_system.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class RefreshToken {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String token;

    @ManyToOne
    private User user;

    private LocalDateTime expiresAt;
    private boolean revoked;
}
