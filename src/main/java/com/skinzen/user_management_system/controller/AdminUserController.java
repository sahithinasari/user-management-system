package com.skinzen.user_management_system.controller;

import com.skinzen.user_management_system.dto.RegisterRequest;
import com.skinzen.user_management_system.exceptions.ApiResponse;
import com.skinzen.user_management_system.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    AdminService adminService;

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody RegisterRequest request) {
        adminService.createUserByAdmin(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse("Registration successful. Please verify your email."));
    }
}
