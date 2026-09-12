package com.skinzen.user_management_system.service;

import com.skinzen.user_management_system.dto.UpdateUserRequest;
import com.skinzen.user_management_system.dto.UserResponse;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
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
