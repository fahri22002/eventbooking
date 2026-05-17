package org.agora.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * This class defines the API metadata and sets up JWT (JSON Web Token)
 * directly from the Swagger UI.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Event Booking API",
                version = "1.0",
                description = "API documentation for the event ticket booking system."
        ),
        security = {@SecurityRequirement(name = "bearerAuth")}
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Enter the JWT token (without the 'Bearer ' text))",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}