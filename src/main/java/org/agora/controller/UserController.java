package org.agora.controller;

import lombok.RequiredArgsConstructor;
import org.agora.dto.UserProfileResponse;
import org.agora.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing User Profile.
 * Handles user profile retrieval.
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * Retrieves an authenticated user profile.
     * @return a {@link ResponseEntity} containing an authenticated user profile.
     */
    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserDetails() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }
}
