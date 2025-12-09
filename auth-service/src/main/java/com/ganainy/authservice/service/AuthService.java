package com.ganainy.authservice.service;

import com.ganainy.authservice.model.dto.AuthResponse;
import com.ganainy.authservice.model.dto.CreateUserRequest;
import com.ganainy.authservice.model.dto.LoginRequest;
import com.ganainy.authservice.model.entity.User;
import com.ganainy.authservice.repository.UserRepository;
import com.ganainy.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService - Handles user registration and authentication.
 * 
 * =====================================================
 * AUTHENTICATION FLOW
 * =====================================================
 * 
 * REGISTRATION:
 * 1. Client sends: POST /api/auth/register { email, password, firstName,
 * lastName }
 * 2. Server creates user with hashed password
 * 3. Server generates JWT token
 * 4. Server returns token + user info
 * 
 * LOGIN:
 * 1. Client sends: POST /api/auth/login { email, password }
 * 2. Server validates credentials (via AuthenticationManager)
 * 3. If valid, server generates JWT token
 * 4. Server returns token + user info
 * 
 * USING THE TOKEN:
 * 1. Client includes token in all requests: Authorization: Bearer <token>
 * 2. JwtAuthenticationFilter validates token
 * 3. If valid, request proceeds to controller
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    /**
     * Register a new user and return a JWT token.
     * 
     * @param request The registration request with user details
     * @return AuthResponse with JWT token and user info
     * @throws IllegalArgumentException if email already exists
     */
    public AuthResponse register(CreateUserRequest request) {
        log.info("Registering new user: {}", request.email());

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed - email already exists: {}", request.email());
            throw new IllegalArgumentException("Email already exists: " + request.email());
        }

        // Create new user with hashed password
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        // Role, enabled, etc. use default values from entity

        // Save to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Generate JWT for the new user
        // We need to create a UserDetails object for JWT generation
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRole().name());
    }

    /**
     * Authenticate a user and return a JWT token.
     * 
     * @param request The login request with email and password
     * @return AuthResponse with JWT token and user info
     * @throws AuthenticationException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.email());

        // AuthenticationManager validates the credentials
        // This will throw an exception if authentication fails
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        // If we get here, authentication was successful
        // Load the user to get their details
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        log.info("User logged in successfully: {}", user.getEmail());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name());
    }
}
