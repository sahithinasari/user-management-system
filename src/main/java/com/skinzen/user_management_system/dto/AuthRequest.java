package com.skinzen.user_management_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(

        @NotBlank(message = "Identifier is required")
        @Email(message = "Please enter a valid email address")
        String identifier,

        @NotBlank(message = "Password is required")
        String password
) {
}
