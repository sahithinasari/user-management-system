package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.dto.UpdateUserRequest;
import com.skinzen.user_management_system.dto.UserResponse;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUser_shouldReturnUser() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setName("Test User");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        UserResponse response =
                userService.getCurrentUser("test@gmail.com");

        assertNotNull(response);
        assertEquals("test@gmail.com", response.email());
        assertEquals("Test User", response.name());
    }

    @Test
    void updateCurrentUser_shouldUpdateName() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setName("Old Name");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequest request =
                new UpdateUserRequest("New Name", "1234567890");

        UserResponse response = userService.updateCurrentUser(
                "test@gmail.com",
                request
        );

        assertEquals("New Name", user.getName());
        assertEquals("1234567890", response.mobileNo());
        verify(userRepository).save(user);
    }
}