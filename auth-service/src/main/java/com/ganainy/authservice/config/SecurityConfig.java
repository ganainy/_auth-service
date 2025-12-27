package com.ganainy.authservice.config;

import com.ganainy.authservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig - Complete Spring Security configuration with JWT.
 * 
 * =====================================================
 * WHAT CHANGED FROM WEEK 1?
 * =====================================================
 * 
 * Week 1: permitAll() for everything (no authentication)
 * Week 2: Proper JWT-based authentication with protected endpoints
 * 
 * Key additions:
 * 1. JwtAuthenticationFilter - Validates JWT on every request
 * 2. AuthenticationManager - Handles login credential verification
 * 3. AuthenticationProvider - Connects UserDetailsService + PasswordEncoder
 * 4. SessionCreationPolicy.STATELESS - No server-side sessions (JWT is
 * stateless)
 * 5. @EnableMethodSecurity - Enable @PreAuthorize on methods
 * 
 * =====================================================
 * SECURITY FILTER ORDER
 * =====================================================
 * 
 * Order matters! Our filter chain:
 * 1. CORS filter (if enabled)
 * 2. JwtAuthenticationFilter (our custom filter) ◄── Added here
 * 3. UsernamePasswordAuthenticationFilter (for form login, not used)
 * 4. ExceptionTranslationFilter
 * 5. AuthorizationFilter (final check)
 * 
 * We add JwtAuthenticationFilter BEFORE UsernamePasswordAuthenticationFilter
 * so JWT validation happens early in the chain.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @Secured, @RolesAllowed on methods
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final UserDetailsService userDetailsService;

        /**
         * =====================================================
         * SECURITY FILTER CHAIN
         * =====================================================
         * 
         * This is the main configuration that defines:
         * - Which endpoints are public
         * - Which endpoints require authentication
         * - How authentication is performed
         * - Session management policy
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // Disable CSRF - not needed for stateless JWT authentication
                                // CSRF protection is for cookie-based auth to prevent cross-site attacks
                                // With JWT in headers, CSRF is not a concern
                                .csrf(AbstractHttpConfigurer::disable)

                                // Configure authorization rules
                                .authorizeHttpRequests(auth -> auth
                                                // PUBLIC ENDPOINTS (no authentication required)
                                                // ---------------------------------------------
                                                // Auth endpoints - login and register must be public!
                                                .requestMatchers("/api/v1/auth/**").permitAll()

                                                // H2 Console - for development only
                                                .requestMatchers("/h2-console/**").permitAll()

                                                // Actuator health endpoint (for load balancers)
                                                .requestMatchers("/actuator/health").permitAll()

                                                // Swagger/OpenAPI documentation (will add later)
                                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                                                // PROTECTED ENDPOINTS (authentication required)
                                                // ---------------------------------------------
                                                // All other requests require authentication
                                                .anyRequest().authenticated())

                                // Session Management - STATELESS for JWT
                                // No server-side sessions are created or used
                                // Each request is independent and authenticated via JWT
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Set our custom authentication provider
                                .authenticationProvider(authenticationProvider())

                                // Add JWT filter BEFORE UsernamePasswordAuthenticationFilter
                                // This ensures JWT is validated early in the filter chain
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                                // Allow H2 console frames (H2 console uses iframes)
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

                return http.build();
        }

        /**
         * =====================================================
         * AUTHENTICATION PROVIDER
         * =====================================================
         * 
         * DaoAuthenticationProvider connects:
         * - UserDetailsService (loads user from database)
         * - PasswordEncoder (compares passwords)
         * 
         * When AuthenticationManager.authenticate() is called:
         * 1. Provider calls UserDetailsService.loadUserByUsername()
         * 2. Provider calls PasswordEncoder.matches(rawPassword, hashedPassword)
         * 3. If both succeed, returns authenticated user
         * 4. If either fails, throws BadCredentialsException
         * 
         * Spring Security 7.0 Note: DaoAuthenticationProvider now takes
         * UserDetailsService
         * in the constructor rather than via setter.
         */
        @Bean
        public AuthenticationProvider authenticationProvider() {
                // Spring Security 7.0+: Use constructor injection
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        /**
         * =====================================================
         * AUTHENTICATION MANAGER
         * =====================================================
         * 
         * AuthenticationManager is the main entry point for authentication.
         * It delegates to one or more AuthenticationProviders.
         * 
         * We use Spring's default configuration which picks up our
         * AuthenticationProvider bean automatically.
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
                        throws Exception {
                return config.getAuthenticationManager();
        }

        /**
         * Password encoder bean - BCrypt for secure password hashing.
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
