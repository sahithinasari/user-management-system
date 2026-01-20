package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.exceptions.JwtAuthenticationException;
import com.skinzen.user_management_system.model.RefreshToken;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public RefreshToken validateAndGet(String tokenValue) {

        RefreshToken token = repository.findByToken(tokenValue)
                .orElseThrow(() -> new JwtAuthenticationException("Invalid refresh token"));

        if (token.isRevoked()) {
            throw new JwtAuthenticationException("Invalid refresh token");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new JwtAuthenticationException("Refresh token expired");
        }

        return token;
    }

    @Transactional
    public void revoke(String tokenValue) {

        repository.findByToken(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            repository.save(token);
        });

        // Do NOT throw if token is missing
        // Idempotent logout
    }
    public String createRefreshToken(User user) {

        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        token.setRevoked(false);

        repository.save(token);
        return token.getToken();
    }
}

