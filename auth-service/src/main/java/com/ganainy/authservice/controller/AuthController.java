package com.ganainy.authservice.controller;

<<<<<<< HEAD
import com.ganainy.authservice.model.dto.AuthResponse;
import com.ganainy.authservice.model.dto.CreateUserRequest;
import com.ganainy.authservice.model.dto.LoginRequest;
import com.ganainy.authservice.service.AuthService;
=======
import com.ganainy.authservice.model.dto.AuthenticationResponse;
import com.ganainy.authservice.model.dto.LoginRequest;
import com.ganainy.authservice.model.dto.UserRegistrationRequest;
import com.ganainy.authservice.service.AuthenticationService;
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
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
<<<<<<< HEAD
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
=======
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
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
@RequiredArgsConstructor
@Slf4j
public class AuthController {

<<<<<<< HEAD
    private final AuthService authService;
=======
    private final AuthenticationService authenticationService;
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963

    /**
     * Register a new user.
     * 
<<<<<<< HEAD
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
=======
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
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
<<<<<<< HEAD
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
=======
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
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
}
