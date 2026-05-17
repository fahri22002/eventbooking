package org.agora.service;

import lombok.RequiredArgsConstructor;
import org.agora.dto.*;
import org.agora.entity.User;
import org.agora.repository.UserRepository;
import org.agora.security.JwtService;
import org.agora.exception.DuplicateResourceException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Service layer responsible for user authentication and registration.
 * Handles secure password hashing, duplicate email validation, and JWT generation.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system after validating email uniqueness.
     *
     * @param request the registration details provided by the user.
     * @return the newly created user profile.
     * @throws DuplicateResourceException if the email is already registered.
     */
    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .createAt(ZonedDateTime.now())
                .build();

        userRepository.save(user);

        return new UserProfileResponse(user.getUserId(), user.getName(), user.getEmail(), user.getCreateAt());
    }

    /**
     * Authenticates user credentials and issues a JSON Web Token (JWT) upon success.
     *
     * @param request the login credentials (email and password).
     * @return an {@link AuthResponse} containing the generated JWT.
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        var jwtToken = jwtService.generateToken(user.getEmail());
        return new AuthResponse(jwtToken);
    }
}