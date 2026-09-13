package com.skinzen.user_management_system.dto;

import com.skinzen.user_management_system.enums.Role;
import com.skinzen.user_management_system.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        UserStatus status,
        boolean emailVerified,
        Role role,
        String mobileNo,
        LocalDateTime createdAt
) {
}