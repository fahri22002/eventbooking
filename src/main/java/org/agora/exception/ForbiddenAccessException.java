package org.agora.exception;

/**
 * Custom runtime exception thrown when user want to change other's authenticated data (e.g. other's event)
 * Intercepted by the {@link GlobalExceptionHandler} to return a 403 Forbidden status.
 */
public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException(String message) {
        super(message);
    }
}