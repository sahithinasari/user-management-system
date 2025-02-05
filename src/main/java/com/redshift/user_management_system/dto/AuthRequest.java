package com.redshift.user_management_system.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String identifier;
    private String password;
}
