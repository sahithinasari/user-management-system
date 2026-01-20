// UserService.java
package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.dto.*;
import com.skinzen.user_management_system.enums.Role;
import com.skinzen.user_management_system.enums.UserStatus;
import com.skinzen.user_management_system.exceptions.JwtAuthenticationException;
import com.skinzen.user_management_system.exceptions.RegistrationException;
import com.skinzen.user_management_system.model.RefreshToken;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.UserRepository;
import com.skinzen.user_management_system.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RegistrationException("Registration failed");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(hashedPassword);
        user.setName(request.getName());
        user.setRole(Role.USER);
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        emailVerificationService.sendVerificationEmail(user);
    }

    @Transactional
    public LoginResponse login(AuthRequest request) throws AuthenticationException {
        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new JwtAuthenticationException("Invalid credentials"));

        if (user.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new JwtAuthenticationException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new LoginResponse(
                accessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpiry()
        );
    }


    @Transactional
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {

        RefreshToken newRefreshToken =
                refreshTokenService.rotate(request.getRefreshToken());

        User user = newRefreshToken.getUser();

        String newAccessToken = jwtUtil.generateAccessToken(user);

        return new RefreshTokenResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                jwtUtil.getAccessTokenExpiry()
        );
    }

    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
    }

}
