package com.ganainy.authservice.security;

import com.ganainy.authservice.model.entity.User;
import com.ganainy.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CustomUserDetailsService - Loads user data from the database.
 * 
 * =====================================================
 * WHAT IS UserDetailsService?
 * =====================================================
 * 
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
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
                });

        log.debug("Found user: {} with role: {}", user.getEmail(), user.getRole());

        // Convert our User entity to Spring Security's UserDetails
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
    }
}
