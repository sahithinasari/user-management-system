package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.enums.UserStatus;
import com.skinzen.user_management_system.mailing.EmailSender;
import com.skinzen.user_management_system.model.EmailVerificationToken;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.EmailVerificationTokenRepository;
import com.skinzen.user_management_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private EmailSender emailSender;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailVerificationService service;

    @Test
    void verifyEmail_shouldActivateUser() {

        User user = new User();
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);

        EmailVerificationToken token =
                new EmailVerificationToken();

        token.setUser(user);
        token.setUsed(false);
        token.setExpiresAt(
                LocalDateTime.now().plusHours(1)
        );

        when(tokenRepository.findByToken("token"))
                .thenReturn(Optional.of(token));

        service.verifyEmail("token");

        assertTrue(user.isEmailVerified());
        assertEquals(UserStatus.ACTIVE, user.getStatus());

        verify(userRepository).save(user);
    }

    @Test
    void verifyEmail_shouldRejectExpiredToken() {
        User user = new User();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(tokenRepository.findByToken("token"))
                .thenReturn(Optional.of(token));

        assertThrows(
                RuntimeException.class,
                () -> service.verifyEmail("token")
        );
    }

}