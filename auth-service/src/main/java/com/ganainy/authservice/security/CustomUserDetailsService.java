package com.ganainy.authservice.security;

import com.ganainy.authservice.model.entity.User;
import com.ganainy.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
<<<<<<< HEAD
=======
import org.springframework.security.core.GrantedAuthority;
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
<<<<<<< HEAD
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CustomUserDetailsService - Loads user data from the database.
=======

import java.util.Collection;
import java.util.List;

/**
 * CustomUserDetailsService - Loads user data for Spring Security.
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
 * 
 * =====================================================
 * WHAT IS UserDetailsService?
 * =====================================================
 * 
<<<<<<< HEAD
 * UserDetailsService is the interface Spring Security uses to load user data.
 * It has a single method: loadUserByUsername(String username)
 * 
 * Spring Security calls this during authentication to:
 * 1. Load the user from wherever you store them (database, LDAP, etc.)
 * 2. Convert your User entity to Spring's UserDetails
 * 3. Verify the password matches
 * 
 * =====================================================
 * WHY CUSTOM IMPLEMENTATION?
 * =====================================================
 * 
 * In Week 1, we used InMemoryUserDetailsManager (test users in memory).
 * Now we need to load users from our PostgreSQL database.
 * 
 * This implementation:
 * 1. Queries the UserRepository for the user by email
 * 2. Converts our User entity to Spring's UserDetails
 * 3. Returns the UserDetails for Spring Security to use
 * 
 * =====================================================
 * UserDetails vs User Entity
 * =====================================================
 * 
 * Our User entity has custom fields (firstName, lastName, etc.).
 * Spring Security only needs what's in UserDetails:
 * - username (we use email)
 * - password (hashed)
 * - authorities (roles/permissions)
 * - account status flags (enabled, locked, expired)
 * 
 * We convert between them in loadUserByUsername().
=======
 * UserDetailsService is a Spring Security interface with ONE method:
 * UserDetails loadUserByUsername(String username)
 * 
 * Spring Security calls this method when:
 * 1. User tries to login (to verify password)
 * 2. JWT is validated (to load user authorities)
 * 3. Any authenticated endpoint is accessed
 * 
 * It's the BRIDGE between your User entity and Spring Security.
 * 
 * =====================================================
 * WHAT IS UserDetails?
 * =====================================================
 * 
 * UserDetails is Spring Security's interface for user information:
 * - getUsername() → User identifier
 * - getPassword() → Hashed password
 * - getAuthorities() → Roles/permissions (ROLE_ADMIN, ROLE_DOCTOR)
 * - isEnabled() → Is account active?
 * - isAccountNonLocked() → Is account locked?
 * - etc.
 * 
 * We need to convert our User entity to something that implements
 * UserDetails so Spring Security can work with it.
 * 
 * =====================================================
 * TWO APPROACHES
 * =====================================================
 * 
 * Approach 1: Create a wrapper class (UserPrincipal) that implements
 * UserDetails
 * class UserPrincipal implements UserDetails {
 * private User user;
 * // Implement all UserDetails methods
 * }
 * 
 * Approach 2: Make User entity implement UserDetails directly
 * class User implements UserDetails {
 * // User already has the methods, just map them
 * }
 * 
 * We'll use Approach 2 for simplicity - let's update User entity to
 * implement UserDetails. But first, let's create this service.
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
 */
@Service
@RequiredArgsConstructor
@Slf4j
<<<<<<< HEAD
@Transactional(readOnly = true)
=======
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
<<<<<<< HEAD
     * Load a user by their email (username in our system).
     * 
     * @param email The email to search for
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        // Find user in database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
=======
     * Load a user by their username (email in our case).
     * 
     * Spring Security calls this automatically:
     * 1. During login: to get the user and verify password
     * 2. During JWT validation: to load authorities for the user
     * 
     * @param username The username (email) to search for
     * @return UserDetails containing user info and authorities
     * @throws UsernameNotFoundException if user doesn't exist
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
                });

        log.debug("Found user: {} with role: {}", user.getEmail(), user.getRole());

        // Convert our User entity to Spring Security's UserDetails
<<<<<<< HEAD
        // We use the built-in User class from Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isCredentialsNonExpired(),
                user.isAccountNonLocked(),
                // Convert role to GrantedAuthority
                // "ROLE_" prefix is required for hasRole() to work
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
=======
        // We'll use Spring's built-in User class as a wrapper
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // username
                user.getPassword(), // hashed password
                user.isEnabled(), // enabled
                user.isAccountNonExpired(), // account not expired
                user.isCredentialsNonExpired(), // credentials not expired
                user.isAccountNonLocked(), // account not locked
                getAuthorities(user) // authorities/roles
        );
    }

    /**
     * Convert our User's role to Spring Security authorities.
     * 
     * =====================================================
     * AUTHORITIES vs ROLES
     * =====================================================
     * 
     * In Spring Security:
     * - ROLE is a special type of authority prefixed with "ROLE_"
     * - hasRole("ADMIN") checks for authority "ROLE_ADMIN"
     * - hasAuthority("ROLE_ADMIN") checks for exact authority "ROLE_ADMIN"
     * 
     * Example:
     * User has role: DOCTOR
     * We create authority: ROLE_DOCTOR
     * 
     * @PreAuthorize("hasRole('DOCTOR')") ✅ Works
     * @PreAuthorize("hasAuthority('ROLE_DOCTOR')") ✅ Works
     * @PreAuthorize("hasAuthority('DOCTOR')") ❌ Doesn't work (no ROLE_ prefix)
     * 
     * Best practice: Always use the "ROLE_" prefix for roles.
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        // Create a single authority based on user's role
        // ROLE_ADMIN, ROLE_DOCTOR, ROLE_NURSE, etc.
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
    }
}
