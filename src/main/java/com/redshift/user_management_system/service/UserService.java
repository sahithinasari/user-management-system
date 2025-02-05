// UserService.java
package com.redshift.user_management_system.service;

import com.redshift.user_management_system.model.User;
import com.redshift.user_management_system.repository.UserRepository;
import com.redshift.user_management_system.sender.RabbitMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RabbitMQSender sender;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElse(null);
    }

    public String registerUser(User user) {
        Optional<User> existingUser = userRepository.findByUsernameOrMailId(user.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists!");
        }
        existingUser = userRepository.findByUsernameOrMailId(user.getMailId());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword())); // ✅ Encode password before saving
        userRepository.save(user);

        // ✅ Use UserDetails for token generation
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return jwtUtil.generateToken(userDetails);
    }
//    public Optional<User> findByMailId(String mailId) {
//        return userRepository.findByMailId(mailId);
//    }

    public User updateUser(Long id, User updatedUser) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            existingUser.setName(updatedUser.getName());
            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User not found with id " + id);
        }
    }

    public boolean deleteUser(Long id) {
         userRepository.deleteById(id);
         return true;
    }
    
}
