package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.enums.UserStatus;
import com.skinzen.user_management_system.mailing.EmailSender;
import com.skinzen.user_management_system.model.EmailVerificationToken;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.EmailVerificationTokenRepository;
import com.skinzen.user_management_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailSender emailSender;
    private final UserRepository userRepository;

    public void sendVerificationEmail(User user) {

        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        token.setUsed(false);

        tokenRepository.save(token);

        String verificationLink =
            "http://localhost:2023/api/v1/auth/verify?token=" + token.getToken();

        emailSender.send(
            user.getEmail(),
            "Verify your email",
            "Click to verify: " + verificationLink
        );
    }

    @Transactional
    public void verifyEmail(String tokenValue) {

        EmailVerificationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (token.isUsed() ||
                token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired or already used");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);

        token.setUsed(true);
        userRepository.save(user);
        tokenRepository.save(token);
    }

}
