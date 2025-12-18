package com.ganainy.authservice.service;

// Spring annotations
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Spring Security for password hashing
import org.springframework.security.crypto.password.PasswordEncoder;

// Lombok for logging
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Our classes
import com.ganainy.authservice.model.entity.User;
import com.ganainy.authservice.repository.UserRepository;

// Java imports
import java.util.List;
import java.util.Optional;

/**
 * UserService - Business Logic Layer for User operations.
 * 
 * =====================================================
 * WHAT IS A SERVICE?
 * =====================================================
 * 
 * A Service class contains your application's BUSINESS LOGIC.
 * It sits between Controllers and Repositories:
 * 
 * Controller → Service → Repository
 * (HTTP) (Logic) (Database)
 * 
 * The Service layer is responsible for:
 * - Business rules ("email must be unique")
 * - Security ("hash passwords before saving")
 * - Orchestration ("save user, then send welcome email")
 * - Transaction management ("if email fails, rollback user creation")
 * 
 * =====================================================
 * WHAT IS @Service?
 * =====================================================
 * 
 * @Service is a Spring stereotype annotation (like @Repository, @Controller).
 *          It does TWO things:
 * 
 *          1. Marks this class as a Spring-managed bean
 *          - Spring creates ONE instance (singleton by default)
 *          - Spring manages its lifecycle
 *          - Can be injected into other beans with @Autowired
 * 
 *          2. Semantic meaning: "This is a service class"
 *          - Tells other developers this class contains business logic
 *          - Helps with component scanning and organization
 * 
 *          Technically, @Service is just a specialized @Component.
 *          You could use @Component instead, but @Service is clearer.
 * 
 *          =====================================================
 *          WHAT IS @Slf4j?
 *          =====================================================
 * 
 * @Slf4j is a Lombok annotation that automatically creates a logger:
 * 
 *        private static final Logger log =
 *        LoggerFactory.getLogger(UserService.class);
 * 
 *        Now you can use: log.info("message"), log.debug("message"),
 *        log.error("message")
 *        Much cleaner than creating the logger manually!
 * 
 *        =====================================================
 *        WHAT IS @RequiredArgsConstructor?
 *        =====================================================
 * 
 * @RequiredArgsConstructor is a Lombok annotation that generates a constructor
 *                          for all FINAL fields. This is the modern,
 *                          recommended way to do dependency injection!
 * 
 *                          Without Lombok, you'd write:
 *                          private final UserRepository userRepository;
 *                          private final PasswordEncoder passwordEncoder;
 * 
 *                          public UserService(UserRepository userRepository,
 *                          PasswordEncoder passwordEncoder) {
 *                          this.userRepository = userRepository;
 *                          this.passwordEncoder = passwordEncoder;
 *                          }
 * 
 *                          With @RequiredArgsConstructor, Lombok generates this
 *                          constructor for you!
 *                          Spring sees the constructor and automatically
 *                          injects the dependencies.
 * 
 *                          Why "final"?
 *                          - Makes fields immutable after construction (safety)
 *                          - Required for @RequiredArgsConstructor to include
 *                          them
 *                          - Shows clear intent: "these dependencies are
 *                          required"
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true) // Default: read-only transactions (optimized for queries)
public class UserService {

    /**
     * =====================================================
     * DEPENDENCY INJECTION (DI) - The Heart of Spring!
     * =====================================================
     * 
     * These fields are "dependencies" - other objects this class needs to work.
     * 
     * WITHOUT Spring (plain Java), you'd create them manually:
     * UserRepository userRepository = new UserRepositoryImpl();
     * PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
     * 
     * Problems with manual creation:
     * - Hard to test (can't easily swap in mocks)
     * - Tight coupling (UserService depends on concrete classes)
     * - Hard to change (want a different encoder? Change every class that uses it)
     * 
     * WITH Spring (Dependency Injection):
     * Spring creates these objects and "injects" them into our class.
     * 
     * Benefits:
     * - Loose coupling (depend on interfaces, not implementations)
     * - Easy testing (inject mock objects for unit tests)
     * - Centralized configuration (change bean definition in one place)
     * - Single instances (Spring reuses the same repository everywhere)
     * 
     * HOW does Spring know what to inject?
     * 1. Spring scans for @Repository, @Service, @Component classes
     * 2. Creates instances (beans) and stores them in the "Application Context"
     * 3. When creating UserService, sees it needs UserRepository
     * 4. Looks in Application Context for a bean of type UserRepository
     * 5. Injects that bean into the constructor
     */
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // =====================================================
    // CREATE OPERATIONS
    // =====================================================

    /**
     * Register a new user.
     * 
     * @Transactional makes this method run in a DATABASE TRANSACTION.
     * 
     *                What is a transaction?
     *                A transaction groups multiple database operations into ONE
     *                atomic unit.
     *                Either ALL operations succeed, or ALL are rolled back.
     * 
     *                Without @Transactional:
     *                1. Save user → SUCCESS
     *                2. Send welcome email → FAILS
     *                Result: User is saved but no email. Inconsistent state!
     * 
     *                With @Transactional:
     *                1. Begin transaction
     *                2. Save user
     *                3. Send welcome email → FAILS
     *                4. ROLLBACK! User is not saved either.
     *                Result: Consistent state - nothing happened.
     * 
     *                Note: @Transactional at class level sets defaults.
     * @Transactional on a method overrides class-level settings.
     * 
     *                readOnly = false (default) allows INSERT, UPDATE, DELETE
     * 
     * @param user The user to register (password should be plain text)
     * @return The saved user with generated ID and hashed password
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional // Override class-level readOnly=true for write operations
    public User registerUser(User user) {
        log.info("Attempting to register user with email: {}", user.getEmail());

        // Business Rule 1: Email must be unique
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Registration failed: Email {} already exists", user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Business Rule 2: Hash the password before saving
        // NEVER store plain text passwords!
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        // Business Rule 3: New users get PATIENT role by default (if not set)
        if (user.getRole() == null) {
            user.setRole(User.Role.PATIENT);
        }

        // Business Rule 4: New accounts are enabled by default
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        // Save to database
        User savedUser = userRepository.save(user);
        log.info("Successfully registered user with ID: {}", savedUser.getId());

        return savedUser;
    }

    // =====================================================
    // READ OPERATIONS
    // =====================================================

    /**
     * Find a user by their ID.
     * 
     * Returns Optional<User> because the user might not exist.
     * This forces the caller to handle the "not found" case explicitly.
     */
    public Optional<User> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    /**
     * Find a user by their email address.
     * 
     * This will be used for:
     * - Login (find user, verify password)
     * - Password reset (find user by email)
     * - Profile lookup
     */
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users (admin function).
     * 
     * ⚠️ CAUTION: In production, this should be paginated!
     * Loading 100,000 users at once would crash the app.
     * We'll add pagination in a later lesson.
     */
    public List<User> findAllUsers() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    /**
     * Get all users with a specific role.
     * 
     * Use case: "Show me all doctors" for appointment scheduling.
     */
    public List<User> findUsersByRole(User.Role role) {
        log.debug("Finding all users with role: {}", role);
        return userRepository.findAllByRole(role);
    }

    /**
     * Get all active users with a specific role.
     * 
     * "Active" means: enabled = true (account not disabled)
     */
    public List<User> findActiveUsersByRole(User.Role role) {
        log.debug("Finding all active users with role: {}", role);
        return userRepository.findAllByRoleAndEnabledTrue(role);
    }

    /**
     * Search users by name (first or last name).
     * 
     * Use case: Admin searching for a user, autocomplete in a search box.
     */
    public List<User> searchUsersByName(String searchTerm) {
        log.debug("Searching users by name: {}", searchTerm);
        return userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                searchTerm, searchTerm);
    }

    /**
     * Check if an email is already registered.
     * 
     * Use case: Real-time validation during registration form.
     */
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    // =====================================================
    // UPDATE OPERATIONS
    // =====================================================

    /**
     * Update an existing user's profile.
     * 
     * Only updates non-sensitive fields (not password or role).
     * 
     * @param id          The user ID to update
     * @param updatedUser The new user data
     * @return The updated user
     * @throws IllegalArgumentException if user not found
     */
    @Transactional
    public User updateUserProfile(Long id, User updatedUser) {
        log.info("Updating profile for user ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: User not found with ID: {}", id);
                    return new IllegalArgumentException("User not found with ID: " + id);
                });

        // Update allowed fields only
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());

        // If email is changing, check it's not already taken
        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new IllegalArgumentException("Email already in use: " + updatedUser.getEmail());
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        // save() will UPDATE because the entity already has an ID
        User savedUser = userRepository.save(existingUser);
        log.info("Successfully updated user ID: {}", id);

        return savedUser;
    }

    /**
     * Change a user's password.
     * 
     * Security best practice: Verify the current password before changing.
     * 
     * @param id              The user ID
     * @param currentPassword The current password (for verification)
     * @param newPassword     The new password (will be hashed)
     * @throws IllegalArgumentException if user not found or current password wrong
     */
    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        log.info("Changing password for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("Password change failed: Incorrect current password for user ID: {}", id);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Hash and save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Successfully changed password for user ID: {}", id);
    }

    /**
     * Disable a user account (soft delete).
     * 
     * In healthcare, we often can't truly delete records (HIPAA compliance).
     * Instead, we "disable" accounts so data is preserved for audits.
     */
    @Transactional
    public void disableUser(Long id) {
        log.info("Disabling user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        user.setEnabled(false);
        userRepository.save(user);
        log.info("Successfully disabled user ID: {}", id);
    }

    /**
     * Enable a user account.
     */
    @Transactional
    public void enableUser(Long id) {
        log.info("Enabling user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        user.setEnabled(true);
        userRepository.save(user);
        log.info("Successfully enabled user ID: {}", id);
    }

    /**
     * Lock a user account (e.g., after too many failed login attempts).
     */
    @Transactional
    public void lockUser(Long id) {
        log.info("Locking user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        user.setAccountNonLocked(false);
        userRepository.save(user);
        log.info("Successfully locked user ID: {}", id);
    }

    /**
     * Unlock a user account.
     */
    @Transactional
    public void unlockUser(Long id) {
        log.info("Unlocking user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        user.setAccountNonLocked(true);
        userRepository.save(user);
        log.info("Successfully unlocked user ID: {}", id);
    }

    // =====================================================
    // DELETE OPERATIONS
    // =====================================================

    /**
     * Hard delete a user (permanent removal).
     * 
     * ⚠️ WARNING: In healthcare, this should rarely be used!
     * HIPAA requires keeping records for audit purposes.
     * Prefer disableUser() for a soft delete.
     */
    @Transactional
    public void deleteUser(Long id) {
        log.warn("HARD DELETING user ID: {} - This action is permanent!", id);

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("Successfully deleted user ID: {}", id);
    }

    // =====================================================
    // STATISTICS / COUNTS
    // =====================================================

    /**
     * Count users by role - useful for admin dashboard.
     */
    public long countUsersByRole(User.Role role) {
        return userRepository.countByRole(role);
    }

    /**
     * Count all active (enabled) users.
     */
    public long countActiveUsers() {
        return userRepository.countByEnabledTrue();
    }
}
