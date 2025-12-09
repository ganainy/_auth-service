package com.ganainy.authservice.controller;

import com.ganainy.authservice.model.dto.AuthResponse;
import com.ganainy.authservice.model.dto.CreateUserRequest;
import com.ganainy.authservice.model.dto.LoginRequest;
import com.ganainy.authservice.service.AuthService;
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
 * POST /api/auth/register - Register a new user
 * Request: { email, password, firstName, lastName }
 * Response: { token, email, firstName, lastName, role }
 * 
 * POST /api/auth/login - Login an existing user
 * Request: { email, password }
 * Response: { token, email, firstName, lastName, role }
 * 
 * =====================================================
 * USAGE EXAMPLE
 * =====================================================
 * 
 * 1. Register a new user:
 * POST /api/auth/register
 * {
 * "email": "john@example.com",
 * "password": "SecurePass123",
 * "firstName": "John",
 * "lastName": "Doe"
 * }
 * 
 * 2. Login:
 * POST /api/auth/login
 * { "email": "john@example.com", "password": "SecurePass123" }
 * 
 * 3. Use the returned token in subsequent requests:
 * GET /api/users
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     * 
     * Creates a new user account a nd returns a JWT token.
     * The user is automatically logged in after registration.
     * 
     * @param request The registration details
     * @return JWT token and user info
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateUserRequest request) {
        log.info("Registration request for: {}", request.email());

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login an existing user.
     * 
     * Validates credentials and returns a JWT token.
     * 
     * @param request The login credentials
     * @return JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for: {}", request.email());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}
