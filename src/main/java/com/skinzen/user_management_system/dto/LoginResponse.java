package com.skinzen.user_management_system.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
