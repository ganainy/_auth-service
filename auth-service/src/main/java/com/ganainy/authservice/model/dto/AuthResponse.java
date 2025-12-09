package com.ganainy.authservice.model.dto;

/**
 * AuthResponse - DTO for authentication responses.
 * 
 * Returns the JWT token and basic user info after successful login.
 */
public record AuthResponse(
        String token,
        String email,
        String firstName,
        String lastName,
        String role) {
}
