package org.agora.controller;

import lombok.RequiredArgsConstructor;
import org.agora.dto.UserProfileResponse;
import org.agora.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserDetails() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }
}
