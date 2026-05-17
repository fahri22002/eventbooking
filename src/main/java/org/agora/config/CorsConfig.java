package org.agora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global configuration class for Cross-Origin Resource Sharing (CORS).
 * This configuration ensures the API can be safely accessed by external frontend clients.
 */
@Configuration
public class CorsConfig {

    /**
     * Configures the CORS mapping rules for all API endpoints.
     * By default, it allows all origins, standard HTTP methods, and all headers.
     *
     * @return a {@link WebMvcConfigurer} instance containing the CORS mappings.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}