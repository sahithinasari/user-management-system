package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.dto.RegisterRequest;
import com.skinzen.user_management_system.enums.UserStatus;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static com.skinzen.user_management_system.enums.Role.ADMIN;

public class AdminService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailVerificationService emailVerificationService;

    @Transactional
    // Admin creates user (ADMIN)
    public void createUserByAdmin(RegisterRequest request) {

        if (!currentUserIsAdmin()) {
            throw new AccessDeniedException("Only admins can create users");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(ADMIN);
        user.setName(request.getName());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        emailVerificationService.sendVerificationEmail(user);
    }

    // --- helpers ---

    private boolean currentUserIsAdmin() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

}
