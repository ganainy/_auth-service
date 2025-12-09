package com.ganainy.authservice.config;

// Spring Security imports
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig - Central configuration for Spring Security.
 * 
 * =====================================================
 * WHAT IS SPRING SECURITY?
 * =====================================================
 * 
 * Spring Security is a powerful framework for:
 * - Authentication (WHO are you?)
 * - Authorization (WHAT can you do?)
 * - Protection against common attacks (CSRF, session fixation, etc.)
 * 
 * How it works (high level):
 * 
 * HTTP Request → Security Filter Chain → Your Controller
 * ↓
 * Check authentication
 * Check authorization
 * Apply security rules
 * 
 * The Security Filter Chain is a series of filters that process
 * every incoming HTTP request before it reaches your controller.
 * 
 * =====================================================
 * UNDERSTANDING @Configuration AND @EnableWebSecurity
 * =====================================================
 * 
 * @Configuration:
 *                 - Marks this class as a source of bean definitions
 *                 - Spring will scan this class for @Bean methods
 *                 - Methods annotated with @Bean will have their return values
 *                 registered as beans in the Spring container
 * 
 * @EnableWebSecurity:
 *                     - Enables Spring Security's web security features
 *                     - Automatically configures the security filter chain
 *                     - Integrates with Spring MVC
 * 
 *                     Without Spring Security, anyone could:
 *                     - Call any endpoint
 *                     - See all user data
 *                     - Delete records without logging in
 * 
 *                     With Spring Security, we control exactly who can do what!
 */
@Configuration // This class provides beans to Spring
@EnableWebSecurity // Enable Spring Security for web applications
public class SecurityConfig {

    /**
     * =====================================================
     * PASSWORD ENCODER - BCryptPasswordEncoder
     * =====================================================
     * 
     * @Bean tells Spring:
     *       "This method returns an object that should be managed by Spring.
     *       Whenever someone needs a PasswordEncoder, give them this one."
     * 
     *       WHY BCrypt?
     * 
     *       BCrypt is a password hashing algorithm designed specifically for
     *       passwords:
     *       1. It's SLOW on purpose (prevents brute-force attacks)
     *       2. It includes a "salt" (random data) automatically
     *       3. It's adaptive (can be made slower as computers get faster)
     * 
     *       How hashing works:
     * 
     *       Password: "MyPassword123"
     *       ↓
     *       BCrypt Hash: "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
     * 
     *       The hash is:
     *       - One-way (can't reverse it to get the password)
     *       - Different each time (salt makes it unique)
     *       - Verifiable (can check if a password matches the hash)
     * 
     *       ⚠️ NEVER store plain text passwords!
     *       If your database is breached, attackers would have all passwords.
     *       With hashes, they only get meaningless strings.
     * 
     *       Plain Java equivalent (what BCrypt does internally):
     * 
     *       // DON'T do this - just for illustration
     *       String salt = generateRandomSalt();
     *       String hash = slowHash(password + salt, 10000 iterations);
     *       return "$2a$10$" + salt + hash;
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with default strength (10 rounds)
        // Higher rounds = more secure but slower
        // 10 rounds takes ~100ms, 12 rounds ~400ms
        return new BCryptPasswordEncoder();
    }

    /**
     * =====================================================
     * IN-MEMORY USER DETAILS SERVICE
     * =====================================================
     * 
     * UserDetailsService is the interface Spring Security uses to
     * load user data for authentication.
     * 
     * There are several implementations:
     * - InMemoryUserDetailsManager: Users stored in memory (for testing)
     * - JdbcUserDetailsManager: Users stored in a database
     * - Custom implementation: Your own logic (we'll do this in Week 2)
     * 
     * For now, we use InMemoryUserDetailsManager to create test users.
     * These users ONLY exist in memory and disappear when the app stops.
     * 
     * In Week 2, we'll replace this with a custom UserDetailsService
     * that loads users from our PostgreSQL database.
     * 
     * User roles explained:
     * - ROLE_ADMIN: Full system access
     * - ROLE_DOCTOR: Medical staff access
     * - ROLE_NURSE: Nursing staff access
     * - ROLE_PATIENT: Patient access only
     * 
     * Note: Spring Security prefixes roles with "ROLE_" internally.
     * When you specify roles("ADMIN"), it becomes "ROLE_ADMIN".
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Create test users with hashed passwords

        // Admin user - has full access
        UserDetails admin = User.builder()
                .username("admin@healthcare.com")
                .password(passwordEncoder.encode("admin123")) // Password is HASHED!
                .roles("ADMIN") // Becomes ROLE_ADMIN
                .build();

        // Doctor user - has medical access
        UserDetails doctor = User.builder()
                .username("doctor@healthcare.com")
                .password(passwordEncoder.encode("doctor123"))
                .roles("DOCTOR")
                .build();

        // Patient user - limited access
        UserDetails patient = User.builder()
                .username("patient@healthcare.com")
                .password(passwordEncoder.encode("patient123"))
                .roles("PATIENT")
                .build();

        // InMemoryUserDetailsManager stores these users in memory
        return new InMemoryUserDetailsManager(admin, doctor, patient);
    }

    /**
     * =====================================================
     * SECURITY FILTER CHAIN - The Heart of Security
     * =====================================================
     * 
     * The SecurityFilterChain defines:
     * 1. Which endpoints require authentication
     * 2. Which roles can access which endpoints
     * 3. How authentication happens (form login, HTTP Basic, JWT, etc.)
     * 4. Security headers and protections
     * 
     * IMPORTANT CONCEPTS:
     * 
     * 1. Authorization Rules (authorizeHttpRequests):
     * - Define WHO can access WHAT
     * - Rules are evaluated TOP TO BOTTOM
     * - First matching rule wins
     * - Always put specific rules before general ones!
     * 
     * 2. HTTP Basic Authentication:
     * - Client sends username:password with each request
     * - Encoded in Base64 in the "Authorization" header
     * - Simple but not very secure (password in every request)
     * - Good for API testing, replaced by JWT in Week 2
     * 
     * 3. Stateless Sessions:
     * - Server doesn't store session information
     * - Each request must include authentication
     * - Required for REST APIs and microservices
     * - Enables horizontal scaling (any server can handle any request)
     * 
     * Example Authorization Header for HTTP Basic:
     * Authorization: Basic YWRtaW5AaGVhbHRoY2FyZS5jb206YWRtaW4xMjM=
     * (This is base64 of "admin@healthcare.com:admin123")
     * 
     * @param http HttpSecurity builder for configuring security
     * @return The configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ========== CSRF Protection ==========
                // CSRF (Cross-Site Request Forgery) protection prevents attackers
                // from tricking users into making unwanted requests.
                //
                // For REST APIs with stateless authentication (like JWT), CSRF is
                // typically disabled because:
                // 1. We don't use cookies for auth (use Authorization header)
                // 2. Each request is independently authenticated
                //
                // ⚠️ WARNING: Only disable CSRF for stateless APIs!
                // For web apps with forms and sessions, keep CSRF enabled!
                .csrf(csrf -> csrf.disable())

                // ========== Authorization Rules ==========
                // Define which endpoints require what authentication/authorization
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        // These are accessible by anyone, even without logging in
                        .requestMatchers(
                                "/api/auth/**", // Login, register endpoints (future)
                                "/api/users/exists", // Check if email exists (for registration)
                                "/actuator/health", // Health check for load balancers
                                "/actuator/info", // Application info
                                "/error" // Error page
                        ).permitAll()

                        // Admin-only endpoints
                        // Only users with ROLE_ADMIN can access these
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // User management requires authentication
                        // Any authenticated user (regardless of role) can access
                        .requestMatchers("/api/users/**").authenticated()

                        // All other endpoints require authentication
                        // This is the "catch-all" rule - put it LAST
                        .anyRequest().authenticated())

                // ========== HTTP Basic Authentication ==========
                // Enable HTTP Basic authentication
                // Client sends: Authorization: Basic <base64(username:password)>
                //
                // We'll replace this with JWT in Week 2, but HTTP Basic is perfect
                // for initial testing and understanding authentication flow.
                .httpBasic(basic -> {
                })

                // ========== Session Management ==========
                // STATELESS means no session is created or used
                // Every request must include authentication credentials
                // This is essential for REST APIs and microservices
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Build and return the security filter chain
        return http.build();
    }
}
