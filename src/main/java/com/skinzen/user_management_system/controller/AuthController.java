package com.skinzen.user_management_system.controller;

import com.skinzen.user_management_system.dto.*;
import com.skinzen.user_management_system.exceptions.ApiResponse;
import com.skinzen.user_management_system.exceptions.TooManyRequestsException;
import com.skinzen.user_management_system.service.AuthService;
import com.skinzen.user_management_system.service.EmailVerificationService;
import com.skinzen.user_management_system.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        if (!rateLimiterService.isAllowed("LOGIN:" + ip)) {
            throw new TooManyRequestsException();
        }

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        String ip = httpRequest.getRemoteAddr();

        if (!rateLimiterService.isAllowed("REGISTER:" + ip)) {
            throw new TooManyRequestsException();
        }

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        "Registration successful. Please verify your email."
                ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                authService.refreshAccessToken(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @Valid @RequestBody LogoutRequest request) {

        authService.logout(request);

        return ResponseEntity.ok(
                new ApiResponse("Logout successful")
        );
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token) {

        emailVerificationService.verifyEmail(token);

        return ResponseEntity.ok("Email verified successfully");
    }
}