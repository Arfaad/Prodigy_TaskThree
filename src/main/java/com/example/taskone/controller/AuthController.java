package com.example.taskone.controller;

import com.example.taskone.dto.AuthResponse;
import com.example.taskone.dto.LoginRequest;
import com.example.taskone.dto.RegisterRequest;
import com.example.taskone.exception.ValidationException;
import com.example.taskone.model.Role;
import com.example.taskone.model.User;
import com.example.taskone.security.JwtUtils;
import com.example.taskone.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller mapping public endpoints for authentication tasks: registering a new user
 * and logging in to retrieve an access token.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Registers a new user. The password is automatically hashed before saving.
     * 
     * @param request The RegisterRequest details.
     * @return The created User entity.
     * @throws ValidationException if email exists or password length validation fails.
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ValidationException("Password is required");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .age(request.getAge())
                .password(request.getPassword())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();

        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * Authenticates credentials and issues a signed JSON Web Token (JWT) on success.
     * 
     * @param request The LoginRequest credentials.
     * @return The AuthResponse containing the Bearer token and user details.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new ValidationException("Email and password are required");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtils.generateToken(user);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(response);
    }
}
