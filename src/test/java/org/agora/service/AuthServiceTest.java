package org.agora.service;

import org.agora.dto.LoginRequest;
import org.agora.dto.RegisterRequest;
import org.agora.dto.AuthResponse;
import org.agora.dto.UserProfileResponse;
import org.agora.entity.User;
import org.agora.repository.UserRepository;
import org.agora.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // MOCKING DEPENDENCIES
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    // INJECT MOCKS
    @InjectMocks
    private AuthService authService;

    /**
     * FR-01 : User Registration Test
     * Success Case
     */
    @Test
    void signUpSuccess() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Fahri Nizar", "fahri@agora.com", "rahasia123");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setEmail(request.email());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserProfileResponse response = authService.register(request);

        // Assert
        assertNotNull(response);

        assertEquals(request.name(), response.name());
        assertEquals(request.email(), response.email());
        assertNotNull(response.userId());
        assertNotNull(response.createAt());
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * FR-01 : User Registration Test
     * Fail Case : Email already exist
     */
    @Test
    void signUpEmailAlreadyExistsThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Fahri Nizar", "fahri@agora.com", "rahasia123");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * FR-02 : User Login
     * Success Case
     */
    @Test
    void signInSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest("fahri@agora.com", "rahasia123");

        User mockUser = new User();
        mockUser.setEmail(request.email());
        mockUser.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser.getEmail())).thenReturn("mockJwtToken");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mockJwtToken", response.token());

        // Make sure AuthenticationManager was called to validate
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
    }

    /**
     * FR-02 : User Login
     * Fail Case : Bad credentials
     */
    @Test
    void signInBadCredentialsThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest("fahri@agora.com", "salah password");

        // Simulasi ketika AuthenticationManager menolak kredensial
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        // Make sure token was never generated
        verify(jwtService, never()).generateToken(any());
    }
}