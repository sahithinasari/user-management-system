package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.dto.ChangePasswordRequest;
import com.skinzen.user_management_system.dto.UpdateUserRequest;
import com.skinzen.user_management_system.dto.UserResponse;
import com.skinzen.user_management_system.exceptions.JwtAuthenticationException;
import com.skinzen.user_management_system.exceptions.RegistrationException;
import com.skinzen.user_management_system.exceptions.UserNotFoundException;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(
            String email,
            UpdateUserRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        user.setName(request.name());
        user.setMobileNo(request.mobileNo());

        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new JwtAuthenticationException("Unauthorized")
                );

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash())) {

            throw new JwtAuthenticationException("Invalid current password");
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPasswordHash())) {

            throw new RegistrationException(
                    "New password must be different from current password"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(request.newPassword())
        );

        userRepository.save(user);

        // Important:
        // invalidate existing refresh-token sessions
        refreshTokenService.revokeAllTokensForUser(user);
    }
    private UserResponse toUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getStatus(),
                user.isEmailVerified(),
                user.getRole(),
                user.getMobileNo(),
                user.getCreatedAt()
        );
    }
}
