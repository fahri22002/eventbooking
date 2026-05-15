package org.agora.service;

import org.agora.dto.UserProfileResponse;
import org.agora.entity.User;
import org.agora.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    /**
     * Cleanup data from previous test
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * FR-03 : Get User Profile
     * Success Case
     */
    @Test
    void getCurrentUserProfileSuccess() {
        // Arrange Mock Security Context
        String mockEmail = "fahri@agora.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(mockEmail);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Arrange database data
        User mockUser = new User();
        mockUser.setUserId(UUID.randomUUID().toString());
        mockUser.setName("Fahri Nizar");
        mockUser.setEmail(mockEmail);
        mockUser.setCreateAt(ZonedDateTime.now());

        when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.of(mockUser));

        // Act
        UserProfileResponse response = userService.getCurrentUserProfile();

        // Assert
        assertNotNull(response);
        assertEquals(mockUser.getName(), response.getName());
        assertEquals(mockEmail, response.getEmail());
        verify(userRepository, times(1)).findByEmail(mockEmail);
    }

    /**
     * FR-03 : Get User Profile
     * Fail Case : User not found
     */
    @Test
    void getCurrentUserProfileUserNotFoundThrowsException() {
        // Arrange Mock Security Context
        String mockEmail = "hacker@agora.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(mockEmail);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Arrange email not found after search
        when(userRepository.findByEmail(mockEmail)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getCurrentUserProfile();
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(mockEmail);
    }

    /**
     * FR-03 : Get User Profile
     * Fail Case : Do not have valid JWT
     */
    @Test
    void getCurrentUserProfileNoJwtThrowsException() {
        // Arrange Mock Security Context To Be Null
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            userService.getCurrentUserProfile();
        });

        // Make sure database never search user if client don't have valid JWT
        verify(userRepository, never()).findByEmail(anyString());
    }
}