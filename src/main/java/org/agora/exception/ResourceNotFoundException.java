package org.agora.exception;

/**
 * Custom runtime exception thrown when user want to access or update data that is not found
 * Intercepted by the {@link GlobalExceptionHandler} to return a 404 Not Found status.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}