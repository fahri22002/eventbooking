package org.agora.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user authentication requests.
 * Encapsulates and validates the user's email and password credentials for login.
 */
public record LoginRequest (    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password cannot be empty")
    String password
) {}
