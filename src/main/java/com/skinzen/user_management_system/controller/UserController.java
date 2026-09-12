package com.skinzen.user_management_system.controller;

import com.skinzen.user_management_system.dto.UpdateUserRequest;
import com.skinzen.user_management_system.dto.UserResponse;
import com.skinzen.user_management_system.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                userService.getCurrentUser(email)
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                userService.updateCurrentUser(email, request)
        );
    }
}
