package com.redshift.user_management_system.service;

import com.redshift.user_management_system.dto.AuthRequest;
import com.redshift.user_management_system.model.Role;
import com.redshift.user_management_system.model.User;
import com.redshift.user_management_system.repository.RoleRepository;
import com.redshift.user_management_system.repository.UserRepository;
import com.redshift.user_management_system.security.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;
    public AuthenticationService(UserRepository userRepository, RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                                 JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // ✅ User Registration (Default Role: USER)
    public String registerUser(User user,String role) {
        if (userRepository.findByUsernameOrMailId(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash password
        
        Role userRole = roleRepository.findByName(role)
                .orElseThrow(() -> new RuntimeException("Default role not found!"));
        
        user.setRoles(new HashSet<>(Collections.singleton(userRole))); // Assign USER role
        userRepository.save(user);

        return "User registered successfully!";
    }

    // ✅ User Login (JWT Token Generation)
    public String loginUser(AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getIdentifier(), authRequest.getPassword())
        );
        User user = userRepository.findByUsernameOrMailId(authRequest.getIdentifier())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return jwtUtil.generateToken(userDetails); // Generate JWT
    }
}
