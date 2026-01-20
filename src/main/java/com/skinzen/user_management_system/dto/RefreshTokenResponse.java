package com.skinzen.user_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RefreshTokenResponse {
    private String accessToken;
    private long expiresIn;
}
