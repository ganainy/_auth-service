package com.ganainy.authservice.service;

import com.ganainy.authservice.exception.InvalidCredentialsException;
import com.ganainy.authservice.mapper.UserMapper;
import com.ganainy.authservice.model.dto.AuthenticationResponse;
import com.ganainy.authservice.model.dto.LoginRequest;
import com.ganainy.authservice.model.dto.UserRegistrationRequest;
import com.ganainy.authservice.model.entity.User;
import com.ganainy.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthenticationService - Handles login and registration logic.
 * 
 * =====================================================
 * LOGIN FLOW
 * =====================================================
 * 
 * 1. User sends email + password to /api/v1/auth/login
 * 2. AuthController calls authService.login(request)
 * 3. AuthenticationService:
 * a. Uses AuthenticationManager to verify credentials
 * b. If valid, loads UserDetails
 * c. Generates JWT token
 * d. Returns token to client
 * 4. Client stores token and uses it for future requests
 * 
 * =====================================================
 * REGISTRATION FLOW
 * =====================================================
 * 
 * 1. User sends registration data to /api/v1/auth/register
 * 2. AuthController calls authService.register(request)
 * 3. AuthenticationService:
 * a. Uses UserService to create the user (validates, hashes password)
 * b. Generates JWT token for immediate login
 * c. Returns token to client
 * 4. Client is immediately logged in (no separate login step needed)
 * 
 * =====================================================
 * AuthenticationManager
 * =====================================================
 * 
 * Spring Security's AuthenticationManager handles the actual
 * credential verification. It:
 * 1. Loads user via UserDetailsService
 * 2. Compares hashed passwords using PasswordEncoder
 * 3. Throws BadCredentialsException if password doesn't match
 * 4. Returns Authentication object if successful
 * 
 * We don't manually check passwords - AuthenticationManager does it
 * using the configured UserDetailsService and PasswordEncoder.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    /**
     * Authenticate a user and return a JWT token.
     * 
     * @param request Login credentials (email, password)
     * @return JWT token and user info
     * @throws InvalidCredentialsException if credentials are wrong
     */
    public AuthenticationResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());

        try {
            // Use AuthenticationManager to verify credentials
            // This internally calls UserDetailsService.loadUserByUsername()
            // and PasswordEncoder.matches() to verify the password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {} - bad credentials", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // If we get here, credentials are valid!
        // Load the user details to generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // Get the actual User entity for the response
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        // Generate JWT token with role claim
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());

        String token = jwtService.generateToken(extraClaims, userDetails);

        log.info("Login successful for user: {}", request.getEmail());

        return AuthenticationResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000) // Convert to seconds
                .user(userMapper.toResponse(user))
                .build();
    }

    /**
     * Register a new user and return a JWT token.
     * 
     * This creates the user AND logs them in immediately.
     * 
     * @param request Registration data
     * @return JWT token and user info
     */
    public AuthenticationResponse register(UserRegistrationRequest request) {
        log.info("Registration attempt for user: {}", request.getEmail());

        // Create the user (UserService handles validation, password hashing, etc.)
        User user = userMapper.toEntity(request);
        User savedUser = userService.registerUser(user);

        // Load user details for token generation
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());

        // Generate JWT token with role claim
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", savedUser.getRole().name());

        String token = jwtService.generateToken(extraClaims, userDetails);

        log.info("Registration successful for user: {}", request.getEmail());

        return AuthenticationResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .user(userMapper.toResponse(savedUser))
                .build();
    }
}
