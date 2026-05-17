package org.agora.exception;

/**
 * Custom runtime exception thrown when a resource already exists (e.g., duplicate email registration).
 * Intercepted by the {@link GlobalExceptionHandler} to return a 409 Conflict status.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}