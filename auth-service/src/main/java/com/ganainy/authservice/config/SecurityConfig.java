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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig - Central configuration for Spring Security with JWT.
 * 
 * =====================================================
 * WEEK 2 UPGRADE: JWT AUTHENTICATION
 * =====================================================
 * 
 * Changes from Week 1:
 * - Removed: InMemoryUserDetailsManager (test users)
 * - Removed: HTTP Basic authentication
 * - Added: JWT token authentication
 * - Added: CustomUserDetailsService (loads users from database)
 * - Added: JwtAuthenticationFilter (validates JWT tokens)
 * - Added: AuthenticationManager (for login endpoint)
 * 
 * =====================================================
 * AUTHENTICATION FLOW (NEW)
 * =====================================================
 * 
 * 1. User calls POST /api/auth/login with email/password
 * 2. AuthenticationManager validates credentials via DaoAuthenticationProvider
 * 3. If valid, server generates JWT token
 * 4. Client includes token in future requests: Authorization: Bearer <token>
 * 5. JwtAuthenticationFilter validates token and sets up SecurityContext
 * 
 * =====================================================
 * FILTER CHAIN ORDER
 * =====================================================
 * 
 * HTTP Request
 * ↓
 * [JwtAuthenticationFilter] ← NEW! Added before UsernamePasswordFilter
 * ↓
 * [UsernamePasswordAuthenticationFilter] (not used directly anymore)
 * ↓
 * [Other Spring Security Filters...]
 * ↓
 * Your Controller
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, @Secured annotations
@RequiredArgsConstructor
public class SecurityConfig {

        // Injected by Spring (CustomUserDetailsService)
        private final UserDetailsService userDetailsService;

        // Injected by Spring (our JWT filter)
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        /**
         * =====================================================
         * PASSWORD ENCODER
         * =====================================================
         * 
         * BCrypt is used for hashing passwords.
         * Same as Week 1 - this doesn't change with JWT.
         */
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        /**
         * =====================================================
         * AUTHENTICATION PROVIDER
         * =====================================================
         * 
         * DaoAuthenticationProvider is a standard Spring Security component that:
         * 1. Uses UserDetailsService to load user from database
         * 2. Uses PasswordEncoder to verify the password
         * 3. Returns authenticated user if credentials are valid
         * 
         * This is used by the AuthenticationManager during login.
         * 
         * Note: Spring Security 7.0 requires passing UserDetailsService to constructor.
         */
        @Bean
        public AuthenticationProvider authenticationProvider() {
                // Spring Security 7.0 requires UserDetailsService in constructor
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

                // Set the password encoder for password verification
                authProvider.setPasswordEncoder(passwordEncoder());

                return authProvider;
        }

        /**
         * =====================================================
         * AUTHENTICATION MANAGER
         * =====================================================
         * 
         * AuthenticationManager is the main entry point for authentication.
         * It delegates to AuthenticationProvider(s) to perform the actual work.
         * 
         * We need this bean to be able to inject it into AuthService
         * for the login endpoint.
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        /**
         * =====================================================
         * SECURITY FILTER CHAIN
         * =====================================================
         * 
         * Main security configuration:
         * - Disable CSRF (stateless API)
         * - Configure public vs protected endpoints
         * - Add JWT filter before UsernamePasswordAuthenticationFilter
         * - Use stateless session management
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // ========== CSRF ==========
                                // Disabled for stateless REST API
                                .csrf(csrf -> csrf.disable())

                                // ========== Authorization Rules ==========
                                .authorizeHttpRequests(auth -> auth
                                                // ===== PUBLIC ENDPOINTS =====
                                                // These can be accessed without authentication
                                                .requestMatchers(
                                                                "/api/auth/**", // Login, register
                                                                "/api/users/exists", // Check email exists
                                                                "/actuator/health", // Health check
                                                                "/actuator/info", // App info
                                                                "/error", // Error pages
                                                                "/swagger-ui/**", // Swagger UI (future)
                                                                "/v3/api-docs/**" // OpenAPI docs (future)
                                                ).permitAll()

                                                // ===== ADMIN-ONLY ENDPOINTS =====
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                                                // ===== AUTHENTICATED ENDPOINTS =====
                                                .requestMatchers("/api/users/**").authenticated()

                                                // ===== ALL OTHER ENDPOINTS =====
                                                .anyRequest().authenticated())

                                // ========== Session Management ==========
                                // STATELESS - no server-side session
                                // Each request is authenticated independently via JWT
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // ========== Authentication Provider ==========
                                // Use our DaoAuthenticationProvider
                                .authenticationProvider(authenticationProvider())

                                // ========== JWT Filter ==========
                                // Add our JWT filter BEFORE the UsernamePasswordAuthenticationFilter
                                // This ensures JWT tokens are processed first
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
