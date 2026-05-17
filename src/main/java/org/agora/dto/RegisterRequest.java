package org.agora.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for new user registration requests.
 * Enforces strict validation rules, including regex-based email format checking.
 */
public record RegisterRequest (

    @NotBlank(message = "Name cannot be empty")
    String name,

    @NotBlank(message = "Email cannot be empty")
    @Email(
            regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Invalid email format"
    )
    String email,

    @NotBlank(message = "Password cannot be empty")
    String password
){}