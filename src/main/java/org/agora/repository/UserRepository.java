package org.agora.repository; // Sesuaikan dengan nama package Anda

import org.agora.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link User} entities.
 * Manages user data access, including email-based lookups and validation for authentication.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Retrieves a user by their registered email address.
     * Essential for Spring Security authentication and user details loading.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if an email address is already registered in the system.
     * Used to prevent duplicate accounts during user registration.
     */
    boolean existsByEmail(String email);
}