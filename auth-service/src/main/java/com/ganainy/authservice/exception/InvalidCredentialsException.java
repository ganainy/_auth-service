package com.ganainy.authservice.exception;

/**
 * InvalidCredentialsException - Thrown when login credentials are invalid.
 * 
 * Maps to HTTP 401 Unauthorized.
 * 
 * Used for:
 * - Wrong password during login
 * - Invalid current password when changing password
 * - Invalid token
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
