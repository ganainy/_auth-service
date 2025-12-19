package com.ganainy.authservice.controller;

import com.ganainy.authservice.model.dto.AuthenticationResponse;
import com.ganainy.authservice.model.dto.LoginRequest;
import com.ganainy.authservice.model.dto.UserRegistrationRequest;
import com.ganainy.authservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API endpoints for authentication.
 * 
 * =====================================================
 * ENDPOINTS
 * =====================================================
 * 
 * POST /api/v1/auth/register - Create new account + get token
 * POST /api/v1/auth/login - Authenticate + get token
 * 
 * These endpoints are PUBLIC (no authentication required).
 * All other endpoints require a valid JWT token.
 * 
 * =====================================================
 * AUTHENTICATION FLOW
 * =====================================================
 * 
 * 1. Register or Login:
 * POST /api/v1/auth/login
 * {"email": "john@hospital.com", "password": "SecurePass123"}
 * 
 * Response:
 * {
 * "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
 * "tokenType": "Bearer",
 * "expiresIn": 3600,
 * "user": {...}
 * }
 * 
 * 2. Use Token for Protected Endpoints:
 * GET /api/v1/users/1
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 * 
 * If token is valid: Request proceeds
 * If token is invalid/missing: 401 Unauthorized
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Register a new user.
     * 
     * Creates a new account and returns a JWT token immediately
     * (no separate login step needed after registration).
     * 
     * HTTP: POST /api/v1/auth/register
     * Request: UserRegistrationRequest (email, password, firstName, lastName)
     * Response: AuthenticationResponse (token + user info)
     * Status: 201 Created
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody UserRegistrationRequest request) {

        log.info("POST /api/v1/auth/register - Registering new user: {}", request.getEmail());

        AuthenticationResponse response = authenticationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticate a user and return a JWT token.
     * 
     * HTTP: POST /api/v1/auth/login
     * Request: LoginRequest (email, password)
     * Response: AuthenticationResponse (token + user info)
     * Status: 200 OK
     * 
     * Error Cases:
     * - Wrong password: 401 Unauthorized
     * - User not found: 401 Unauthorized (same error for security)
     * - Validation error: 400 Bad Request
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("POST /api/v1/auth/login - Login attempt for: {}", request.getEmail());

        AuthenticationResponse response = authenticationService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Simple endpoint to verify if a token is valid.
     * 
     * HTTP: GET /api/v1/auth/me
     * Header: Authorization: Bearer <token>
     * Response: Current user info
     * 
     * This endpoint requires authentication (will be protected by Spring Security).
     * If the token is valid, the user can call this to get their info.
     * If the token is invalid, they'll get 401 Unauthorized.
     */
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        // This will be enhanced later to return the current user's info
        // For now, just return a success message proving authentication works
        return ResponseEntity.ok("You are authenticated!");
    }
}
