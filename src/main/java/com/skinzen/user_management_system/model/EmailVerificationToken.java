package com.skinzen.user_management_system.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
@Data
public class EmailVerificationToken {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    private User user;

    private LocalDateTime expiresAt;

    private boolean used;
}
