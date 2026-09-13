package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.exceptions.JwtAuthenticationException;
import com.skinzen.user_management_system.model.RefreshToken;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void revoke_shouldRevokeAllUserTokens() {
        User user = new User();

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken("refresh-token");

        when(repository.findByToken("refresh-token"))
                .thenReturn(Optional.of(token));

        refreshTokenService.revoke("refresh-token");

        verify(repository).revokeAllByUser(user);
    }

    @Test
    void rotate_shouldRejectMissingToken() {
        when(repository.findByToken("invalid"))
                .thenReturn(Optional.empty());

        assertThrows(
                JwtAuthenticationException.class,
                () -> refreshTokenService.rotate("invalid")
        );
    }

    @Test
    void rotate_shouldRejectExpiredToken() {
        RefreshToken token = new RefreshToken();

        token.setUser(new User());
        token.setRevoked(false);
        token.setExpiresAt(
                LocalDateTime.now().minusMinutes(1)
        );

        when(repository.findByToken("expired"))
                .thenReturn(Optional.of(token));

        assertThrows(
                JwtAuthenticationException.class,
                () -> refreshTokenService.rotate("expired")
        );
    }

    @Test
    void rotate_shouldDetectTokenReuse() {
        User user = new User();

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setRevoked(true);
        token.setExpiresAt(
                LocalDateTime.now().plusDays(1)
        );

        when(repository.findByToken("reused"))
                .thenReturn(Optional.of(token));

        assertThrows(
                JwtAuthenticationException.class,
                () -> refreshTokenService.rotate("reused")
        );

        verify(repository).revokeAllByUser(user);
    }
}