package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.exceptions.JwtAuthenticationException;
import com.skinzen.user_management_system.mailing.EmailSender;
import com.skinzen.user_management_system.model.PasswordResetToken;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.PasswordResetTokenRepository;
import com.skinzen.user_management_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void sendResetEmail(String email) {

        userRepository.findByEmail(email).ifPresent(user -> {

            PasswordResetToken token = new PasswordResetToken();
            token.setToken(UUID.randomUUID().toString());
            token.setUser(user);
            token.setCreatedAt(LocalDateTime.now());
            token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            token.setUsed(false);

            tokenRepository.save(token);

            String resetLink =
                    "http://localhost:2023/api/v1/auth/reset-password?token="
                            + token.getToken();

            emailSender.send(
                    user.getEmail(),
                    "Reset your password",
                    "Click to reset your password: " + resetLink
            );
        });
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {

        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() ->
                        new JwtAuthenticationException("Invalid reset token")
                );

        if (token.isUsed()
                || token.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new JwtAuthenticationException("Invalid reset token");
        }

        User user = token.getUser();

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        // Invalidate all existing refresh-token sessions
        refreshTokenService.revokeAllTokensForUser(user);
    }
}
