package com.redshift.user_management_system.controller;

import com.redshift.user_management_system.dto.AuthRequest;
import com.redshift.user_management_system.model.User;
import com.redshift.user_management_system.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    @Autowired
    private AuthenticationService authenticationService;
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequest authRequest) {
        String token=authenticationService.loginUser(authRequest);
        Map<String, String> response = new HashMap<>();
        response.put("access_token", token);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/register")
    public Map<String, String> registerUser(@RequestBody User user) {
        String message = authenticationService.registerUser(user,"USER");
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

  //  @PreAuthorize("hasRole('ADMIN')") // Only admin can call this
    @PostMapping("/admin/register")
   // public Map<String, String> registerAdmin(@RequestBody User authRequest,  @RequestHeader("Authorization") String adminToken) {
    public Map<String, String> registerAdmin(@RequestBody User authRequest) {
        String message = authenticationService.registerUser(authRequest,"ADMIN");
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
