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
 * JwtAuthenticationFilter - Intercepts requests and validates JWT tokens.
 * 
 * =====================================================
 * HOW THE FILTER CHAIN WORKS
 * =====================================================
 * 
 * When a request comes in, it passes through multiple filters:
 * 
 * HTTP Request
 * ↓
 * [CORS Filter]
 * ↓
 * [CSRF Filter]
 * ↓
 * [JwtAuthenticationFilter] ← WE ARE HERE
 * ↓
 * [UsernamePasswordAuthenticationFilter]
 * ↓
 * [Other Spring Security Filters...]
 * ↓
 * Your Controller
 * 
 * Our filter runs BEFORE the standard authentication filters.
 * If we find a valid JWT, we authenticate the user ourselves.
 * 
 * =====================================================
 * WHAT THIS FILTER DOES
 * =====================================================
 * 
 * 1. Extract the Authorization header from the request
 * 2. Check if it's a Bearer token (starts with "Bearer ")
 * 3. Extract and validate the JWT
 * 4. If valid, load the user and set up authentication
 * 5. Continue the filter chain
 * 
 * =====================================================
 * OncePerRequestFilter
 * =====================================================
 * 
 * We extend OncePerRequestFilter to ensure this filter only runs
 * ONCE per request (even if the request is forwarded internally).
 * 
 * Without this, the filter might run multiple times for a single
 * request, which could cause issues.
 */
@Component
@RequiredArgsConstructor // Lombok: generates constructor for final fields
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Main filter logic - runs for every HTTP request.
     * 
     * @param request     The HTTP request
     * @param response    The HTTP response
     * @param filterChain The chain of filters to continue
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // ========== STEP 1: Get the Authorization header ==========
        final String authHeader = request.getHeader("Authorization");

        // If no Authorization header or not a Bearer token, skip this filter
        // The request will continue to other filters and may fail authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found in Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        // ========== STEP 2: Extract the JWT token ==========
        // Format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
        // We want just the token part (after "Bearer ")
        final String jwt = authHeader.substring(7);

        log.debug("JWT token found, attempting to validate");

        try {
            // ========== STEP 3: Extract username from token ==========
            final String userEmail = jwtService.extractUsername(jwt);

            // ========== STEP 4: Validate and authenticate ==========
            // Only process if:
            // 1. We extracted a username from the token
            // 2. User is not already authenticated
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user from database (or in-memory storage)
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Validate the token against the user
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.debug("JWT token is valid for user: {}", userEmail);

                    // ========== STEP 5: Create authentication token ==========
                    // UsernamePasswordAuthenticationToken represents a successful authentication
                    // Parameters: principal (user), credentials (null for JWT), authorities (roles)
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials not needed after authentication
                            userDetails.getAuthorities());

                    // Add request details (IP address, session ID, etc.)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // ========== STEP 6: Set authentication in SecurityContext ==========
                    // This is THE KEY step - it tells Spring Security the user is authenticated
                    // All subsequent security checks will use this authentication
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("User {} authenticated successfully via JWT", userEmail);
                } else {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            // Token parsing failed - could be expired, malformed, wrong signature, etc.
            log.warn("JWT authentication failed: {}", e.getMessage());
            // Don't throw - let the request continue (will fail at authorization if needed)
        }

        // ========== STEP 7: Continue the filter chain ==========
        // Whether we authenticated or not, pass the request to the next filter
        filterChain.doFilter(request, response);
    }
}
