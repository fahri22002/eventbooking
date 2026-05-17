package org.agora.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.agora.dto.*;
import org.agora.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for handling user authentication.
 * This class manages new user registration and the issuance of JWT tokens for secure API access.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user in the system.
     * @param request the user registration details containing name, email, and password.
     * @return a {@link ResponseEntity} containing the created user profile and a 201 (Created) status.
     */
    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticates a user and generates a JSON Web Token (JWT).
     * @param request the login credentials containing email and password.
     * @return a {@link ResponseEntity} containing the authentication token and a 200 (OK) status.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}