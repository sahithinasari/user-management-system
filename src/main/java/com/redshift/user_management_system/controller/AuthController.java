package com.redshift.user_management_system.controller;

import com.redshift.user_management_system.dto.AuthRequest;
import com.redshift.user_management_system.model.User;
import com.redshift.user_management_system.security.CustomUserDetailsService;
import com.redshift.user_management_system.service.JwtUtil;
import com.redshift.user_management_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody AuthRequest authRequest) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getIdentifier(), authRequest.getPassword())
        );
        // Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getIdentifier());

        // Generate JWT Token
        String token = jwtUtil.generateToken(userDetails);

        // Return Token
        Map<String, String> response = new HashMap<>();
        response.put("access_token", token);
        return response;
    }
    @PostMapping("/register")
    public Map<String, String> registerUser(@RequestBody User user) {
        // Call the UserService to register the user
        String token = userService.registerUser(user);
        // Return the generated JWT token
        Map<String, String> response = new HashMap<>();
        response.put("access_token", token);
        return response;
    }
}
