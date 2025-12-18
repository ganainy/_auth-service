package com.ganainy.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig - Configuration class for security-related beans.
 * 
 * =====================================================
 * WHAT IS @Configuration?
 * =====================================================
 * 
 * @Configuration marks this class as a SOURCE OF BEAN DEFINITIONS.
 * 
 *                Think of it as a "factory" that tells Spring:
 *                "Here are some objects I want you to create and manage."
 * 
 *                Spring processes @Configuration classes at startup and:
 *                1. Finds all @Bean methods
 *                2. Calls each method ONCE
 *                3. Stores the returned objects in the Application Context
 *                4. These objects can now be @Autowired anywhere
 * 
 *                =====================================================
 *                WHAT IS @Bean?
 *                =====================================================
 * 
 * @Bean marks a method that CREATES a Spring-managed object.
 * 
 *       The method name becomes the bean name (by default).
 *       The return type determines which type requests this bean satisfies.
 * 
 *       Example:
 * @Bean
 *       public PasswordEncoder passwordEncoder() { ... }
 * 
 *       This creates a bean:
 *       - Name: "passwordEncoder"
 *       - Type: PasswordEncoder
 *       - Singleton: Only ONE instance exists
 * 
 *       Now anywhere you @Autowire PasswordEncoder, you get this instance!
 * 
 *       =====================================================
 *       WHY USE @Configuration + @Bean?
 *       =====================================================
 * 
 *       Use @Configuration/@Bean when:
 *       - You can't annotate the class (it's from a library)
 *       - You need to customize how the object is created
 *       - You want to create multiple beans of the same type
 * 
 *       Use @Component/@Service/@Repository when:
 *       - You wrote the class yourself
 *       - Default constructor is fine
 *       - Only one instance is needed
 */
@Configuration
@EnableWebSecurity // Enables Spring Security's web security support
public class SecurityConfig {

    /**
     * =====================================================
     * SECURITY FILTER CHAIN - The Core of Spring Security
     * =====================================================
     * 
     * SecurityFilterChain defines HOW to secure HTTP requests.
     * 
     * Spring Security works by adding FILTERS to the request pipeline:
     * Request → [Security Filters] → Controller → Response
     * 
     * These filters check:
     * - Is the user authenticated?
     * - Is the user authorized for this endpoint?
     * - Is CSRF protection needed?
     * - etc.
     * 
     * By DEFAULT, Spring Security:
     * ❌ Blocks ALL requests (returns 401 Unauthorized)
     * ❌ Enables CSRF protection
     * ❌ Requires login for everything
     * 
     * For development/testing, we OVERRIDE this to permit all requests.
     * ⚠️ WARNING: In Week 2, we'll add proper JWT authentication!
     * 
     * @param http The HttpSecurity builder
     * @return Configured SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for now (needed for POST/PUT/DELETE from REST clients)
                // We'll use JWT tokens instead of CSRF tokens for API security
                .csrf(AbstractHttpConfigurer::disable)

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Permit ALL requests for now (development mode)
                        // In Week 2, we'll change this to:
                        // .requestMatchers("/api/v1/auth/**").permitAll()
                        // .anyRequest().authenticated()
                        .anyRequest().permitAll());

        return http.build();
    }

    /**
     * =====================================================
     * PASSWORD ENCODER BEAN
     * =====================================================
     * 
     * Creates a BCryptPasswordEncoder for hashing passwords.
     * 
     * WHAT IS BCrypt?
     * 
     * BCrypt is a password hashing algorithm that:
     * 1. Is SLOW (intentionally!) - makes brute force attacks expensive
     * 2. Uses a SALT - prevents rainbow table attacks
     * 3. Is ADAPTIVE - can increase difficulty over time
     * 
     * How it works:
     * Input: "MyPassword123"
     * Output: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     * |__|___|____________________________________________________|
     * Version Cost Factor Hashed Password
     * (2a) (10 rounds) (includes salt + hash)
     * 
     * The "10" is the cost factor (2^10 = 1024 hash iterations).
     * Higher = more secure but slower. 10 is a good default.
     * 
     * WHY NOT plain hashes like MD5 or SHA?
     * MD5("password") = "5f4dcc3b5aa765d61d8327deb882cf99" (always same!)
     * BCrypt("password") = "$2a$10$..." (different every time due to salt!)
     * 
     * Same password, different bcrypt results:
     * BCrypt("test") → "$2a$10$Ab..."
     * BCrypt("test") → "$2a$10$Xy..." (different salt!)
     * 
     * But BCrypt.matches("test", "$2a$10$Ab...") → true
     * And BCrypt.matches("test", "$2a$10$Xy...") → true
     * 
     * The salt is embedded in the hash, so verification still works!
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Create BCrypt encoder with default strength (10)
        // Strength can be 4-31, higher = more secure but slower
        // 10 is recommended for most applications
        return new BCryptPasswordEncoder();
    }
}
