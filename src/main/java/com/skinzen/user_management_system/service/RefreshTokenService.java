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

    @Transactional
    public void revoke(String tokenValue) {

        repository.findByToken(tokenValue).ifPresent(token -> {
            User user=token.getUser();
            revokeAllTokensForUser(user);
        });

        // Do NOT throw if token is missing
        // Idempotent logout
    }

    @Transactional
    public RefreshToken rotate(String oldTokenValue) {

        RefreshToken oldToken = repository.findByToken(oldTokenValue)
                .orElseThrow(() -> new JwtAuthenticationException("Invalid refresh token"));

        // Reuse detection
        if (oldToken.isRevoked()) {
            // Token reuse detected → possible theft
            revokeAllTokensForUser(oldToken.getUser());
            throw new JwtAuthenticationException("Invalid refresh token");
        }

        if (oldToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new JwtAuthenticationException("Invalid refresh token");
        }

        // Revoke old token
        oldToken.setRevoked(true);
        // oldToken.setRevokedAt(LocalDateTime.now());  // optional
        repository.save(oldToken);
        return createRefreshToken(oldToken.getUser());
    }

    private void revokeAllTokensForUser(User user) {
        repository.revokeAllByUser(user);
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusDays(7));
        token.setRevoked(false);
        return repository.save(token);
    }
}

