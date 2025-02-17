package com.redshift.user_management_system.controller;

import com.redshift.user_management_system.model.User;
import com.redshift.user_management_system.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ✅ Any authenticated user can access their own profile
    @GetMapping("/profile")
    public User getProfile(Principal principal) {
        return userService.getUserByUsername(principal.getName());
    }

    // ✅ Only users with 'USER' role can update their own profile
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/update")
    public User updateProfile(Principal principal, @RequestBody User user) {
        return userService.updateUserProfile(principal.getName(), user);
    }

    // ✅ Only ADMIN users can get a list of all users
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public Iterable<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // ✅ Any authenticated user can delete their own account
    @DeleteMapping("/delete")
    public String deleteAccount(Principal principal) {
        userService.deleteUserByUsername(principal.getName());
        return "User deleted successfully";
    }
}
