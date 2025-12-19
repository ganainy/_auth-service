package com.ganainy.authservice.security;

import com.ganainy.authservice.model.entity.User;
import com.ganainy.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * CustomUserDetailsService - Loads user data for Spring Security.
 * 
 * =====================================================
 * WHAT IS UserDetailsService?
 * =====================================================
 * 
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
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
                });

        log.debug("Found user: {} with role: {}", user.getEmail(), user.getRole());

        // Convert our User entity to Spring Security's UserDetails
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
    }
}
