package com.skinzen.user_management_system.controller;

import com.skinzen.user_management_system.dto.RegisterRequest;
import com.skinzen.user_management_system.exceptions.ApiResponse;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(
            @Valid @RequestBody RegisterRequest request) {

        adminService.createUserByAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        "User created successfully. Please verify the email."
                ));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(
            @PathVariable UUID id) {

        adminService.delete(id);

        return ResponseEntity.ok(
                new ApiResponse("User deleted successfully")
        );
    }
}
