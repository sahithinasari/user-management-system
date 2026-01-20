package com.skinzen.user_management_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequest {
    @NotBlank
    @Email
    private String identifier;

    @NotBlank
    private String password;

    private String remoteAddr;
}
