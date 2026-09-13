package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.dto.AuthRequest;
import com.skinzen.user_management_system.dto.LoginResponse;
import com.skinzen.user_management_system.dto.RegisterRequest;
import com.skinzen.user_management_system.enums.Role;
import com.skinzen.user_management_system.enums.UserStatus;
import com.skinzen.user_management_system.exceptions.JwtAuthenticationException;
import com.skinzen.user_management_system.exceptions.RegistrationException;
import com.skinzen.user_management_system.model.RefreshToken;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.UserRepository;
import com.skinzen.user_management_system.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUser() {
        // given
        RegisterRequest request =
                new RegisterRequest("test@gmail.com", "password", "Sahithi", null, null);

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.password()))
                .thenReturn("hashedPassword");

        // when
        authService.register(request);

        // then
        verify(userRepository).save(any(User.class));
        verify(emailVerificationService)
                .sendVerificationEmail(any(User.class));
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        RegisterRequest request =
                new RegisterRequest("test@gmail.com", "password", "Sahithi", null, null);

        when(userRepository.existsByEmail(request.email()))
                .thenReturn(true);

        assertThrows(
                RegistrationException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokensForValidUser() {
        AuthRequest request =
                new AuthRequest("test@gmail.com", "password");

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPasswordHash("hashed");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.USER);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password", "hashed"))
                .thenReturn(true);

        when(jwtUtil.generateAccessToken(user))
                .thenReturn("access-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");

        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(refreshToken);

        when(jwtUtil.getAccessTokenExpiry())
                .thenReturn(900L);

        LoginResponse response =
                authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void login_shouldRejectInvalidPassword() {
        AuthRequest request =
                new AuthRequest("test@gmail.com", "wrong");

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPasswordHash("hashed");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "hashed"))
                .thenReturn(false);

        assertThrows(
                JwtAuthenticationException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void login_shouldRejectInactiveUser() {
        AuthRequest request =
                new AuthRequest("test@gmail.com", "password");

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setStatus(UserStatus.LOCKED);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                JwtAuthenticationException.class,
                () -> authService.login(request)
        );
    }
}