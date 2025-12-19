package com.ganainy.authservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Validates JWT tokens on every request.
 * 
 * =====================================================
 * WHAT IS A FILTER?
 * =====================================================
 * 
 * A Filter is code that runs BEFORE your controller receives a request.
 * Filters are part of the Servlet specification (not Spring-specific).
 * 
 * Request Flow:
 * Client → [Filter 1] → [Filter 2] → [Filter N] → Controller
 * ↓
 * Client ← [Filter 1] ← [Filter 2] ← [Filter N] ← Controller
 * 
 * Each filter can:
 * - Inspect/modify the request
 * - Reject the request (don't call next filter)
 * - Inspect/modify the response
 * - Perform logging, authentication, etc.
 * 
 * =====================================================
 * SPRING SECURITY FILTER CHAIN
 * =====================================================
 * 
 * Spring Security adds many filters automatically:
 * 1. SecurityContextPersistenceFilter - Load/save security context
 * 2. UsernamePasswordAuthenticationFilter - Form login
 * 3. BasicAuthenticationFilter - HTTP Basic auth
 * 4. JwtAuthenticationFilter - OUR FILTER (custom)
 * 5. ExceptionTranslationFilter - Convert auth exceptions
 * 6. FilterSecurityInterceptor - Final authorization check
 * 
 * Our filter runs BEFORE the authorization check to:
 * 1. Extract the JWT from the Authorization header
 * 2. Validate the token (signature, expiration)
 * 3. Load the user from the database
 * 4. Set up Spring Security's "authentication" context
 * 
 * =====================================================
 * OncePerRequestFilter
 * =====================================================
 * 
 * We extend OncePerRequestFilter to ensure our filter runs
 * exactly ONCE per request (even if request is forwarded).
 * 
 * Without this, a request could be filtered multiple times
 * during internal redirects, causing issues.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Main filter method - runs on EVERY request.
     * 
     * This method:
     * 1. Extracts JWT from Authorization header
     * 2. Validates the token
     * 3. Loads user details
     * 4. Sets up security context (tells Spring Security: "user is authenticated")
     * 5. Continues to the next filter
     * 
     * @param request     HTTP request
     * @param response    HTTP response
     * @param filterChain Chain of remaining filters to execute
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // =========================================
        // STEP 1: Extract Authorization header
        // =========================================
        final String authHeader = request.getHeader("Authorization");

        // Check if Authorization header exists and starts with "Bearer "
        // Format: "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No valid auth header - continue without authentication
            // This is OK for public endpoints (like /api/v1/auth/login)
            // Protected endpoints will be rejected later by Spring Security
            filterChain.doFilter(request, response);
            return;
        }

        // =========================================
        // STEP 2: Extract the token (remove "Bearer " prefix)
        // =========================================
        final String jwt = authHeader.substring(7); // "Bearer ".length() = 7

        log.debug("JWT found in request to: {}", request.getRequestURI());

        try {
            // =========================================
            // STEP 3: Extract username from token
            // =========================================
            final String userEmail = jwtService.extractUsername(jwt);

            // =========================================
            // STEP 4: Validate and authenticate
            // =========================================
            // Only authenticate if:
            // - We extracted a username
            // - User is not already authenticated (context is empty)
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user from database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Validate the token against the user
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // =========================================
                    // STEP 5: Set up authentication context
                    // =========================================
                    // Create an authentication token (not JWT, but Spring's auth object)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, // Principal (the authenticated user)
                            null, // Credentials (null - we used JWT, not password)
                            userDetails.getAuthorities() // Authorities (ROLE_DOCTOR, etc.)
                    );

                    // Add request details (IP address, session ID, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set the authentication in the security context
                    // This tells Spring Security: "This user is authenticated!"
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("User authenticated: {} with authorities: {}",
                            userEmail, userDetails.getAuthorities());
                } else {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.warn("Could not set user authentication in security context: {}", e.getMessage());
            // Don't throw - just continue without authentication
            // Spring Security will reject the request if endpoint requires auth
        }

        // =========================================
        // STEP 6: Continue to the next filter
        // =========================================
        filterChain.doFilter(request, response);
    }
}
