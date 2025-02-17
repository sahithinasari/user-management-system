package com.redshift.user_management_system.service;

import com.redshift.user_management_system.model.User;
import com.redshift.user_management_system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsernameOrMailId(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUserProfile(String username, User updatedUser) {
        return userRepository.findByUsernameOrMailId(username)
                .map(user -> {
                    user.setName(updatedUser.getName());
                    user.setMailId(updatedUser.getMailId());
                    user.setMobileNo(updatedUser.getMobileNo());
                    user.setPassword(passwordEncoder.encode(updatedUser.getPassword())); // Ensure password is hashed
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUserByUsername(String username) {
        userRepository.deleteByUsername(username);
    }
}
