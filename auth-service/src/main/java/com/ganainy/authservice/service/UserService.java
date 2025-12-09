package com.ganainy.authservice.service;

import com.ganainy.authservice.model.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * UserService - Interface defining the business operations for User management.
 * 
 * =====================================================
 * WHY USE AN INTERFACE?
 * =====================================================
 * 
 * 1. **Abstraction**: The interface defines WHAT operations exist,
 * not HOW they're implemented. This allows:
 * - Multiple implementations (e.g., UserServiceImpl, MockUserService)
 * - Easy testing (mock the interface)
 * - Flexibility to swap implementations
 * 
 * 2. **SOLID Principles**:
 * - Dependency Inversion: Depend on abstractions, not concretions
 * - Interface Segregation: Small, focused interfaces
 * 
 * 3. **Spring Best Practice**: In enterprise applications, we always
 * define service interfaces. The controller depends on the interface,
 * not the implementation.
 * 
 * In plain Java, you'd have to manage object creation:
 * UserService service = new UserServiceImpl(new UserDaoImpl());
 * 
 * With Spring's dependency injection:
 * 
 * @Autowired
 *            private UserService userService; // Spring finds & injects the
 *            implementation!
 */
public interface UserService {

    /**
     * Create a new user.
     * 
     * @param user The user to create (without an ID)
     * @return The created user (with generated ID)
     * @throws IllegalArgumentException if email already exists
     */
    User createUser(User user);

    /**
     * Find a user by their ID.
     * 
     * @param id The user's ID
     * @return Optional containing the user, or empty if not found
     */
    Optional<User> findUserById(Long id);

    /**
     * Find a user by their email address.
     * 
     * @param email The email to search for
     * @return Optional containing the user, or empty if not found
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Get all users.
     * 
     * Note: In production with many users, use pagination instead!
     * 
     * @return List of all users
     */
    List<User> findAllUsers();

    /**
     * Update an existing user.
     * 
     * @param id   The ID of the user to update
     * @param user The new user data
     * @return The updated user
     * @throws RuntimeException if user not found
     */
    User updateUser(Long id, User user);

    /**
     * Delete a user by ID.
     * 
     * @param id The ID of the user to delete
     * @throws RuntimeException if user not found
     */
    void deleteUser(Long id);

    /**
     * Check if an email is already registered.
     * 
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    boolean emailExists(String email);
}
