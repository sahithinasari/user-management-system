package com.skinzen.user_management_system.model;

import com.skinzen.user_management_system.enums.Role;
import com.skinzen.user_management_system.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private boolean emailVerified;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String mobileNo;
    private LocalDateTime createdAt;

}

