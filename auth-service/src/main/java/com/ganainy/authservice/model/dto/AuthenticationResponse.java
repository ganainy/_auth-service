package com.ganainy.authservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthenticationResponse - DTO for login response.
 * 
 * Returns the JWT token and basic user info after successful authentication.
 * Used by: POST /api/v1/auth/login, POST /api/v1/auth/register
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    /**
     * The JWT access token.
     * 
     * Client should store this and send it with every request:
     * Authorization: Bearer <token>
     */
    private String accessToken;

    /**
     * Token type (always "Bearer" for JWT).
     */
    private String tokenType;

    /**
     * Token expiration time in seconds.
     */
    private long expiresIn;

    /**
     * Basic user info for convenience.
     */
    private UserResponse user;
}
