package com.ganainy.authservice.service.impl;

import com.ganainy.authservice.model.entity.User;
import com.ganainy.authservice.repository.UserRepository;
import com.ganainy.authservice.service.UserService;

// Lombok annotation for logging - generates: private static final Logger log = LoggerFactory.getLogger(...)
import lombok.extern.slf4j.Slf4j;

// Spring annotations for dependency injection and transaction management
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * UserServiceImpl - Implementation of UserService with business logic.
 * 
 * =====================================================
 * WHAT IS THE SERVICE LAYER?
 * =====================================================
 * 
 * The Service layer sits between the Controller (handles HTTP) and
 * the Repository (handles database). It contains BUSINESS LOGIC.
 * 
 * Layer Architecture:
 * 
 * Controller → Service → Repository → Database
 * ↓ ↓ ↓ ↓
 * HTTP Business Data Storage
 * handling logic access
 * 
 * Why separate layers?
 * - Single Responsibility: Each layer has one job
 * - Testability: Mock dependencies for unit tests
 * - Maintainability: Change business logic without touching HTTP handling
 * - Reusability: Same service can be used by REST API, GraphQL, CLI, etc.
 * 
 * =====================================================
 * UNDERSTANDING THE ANNOTATIONS
 * =====================================================
 * 
 * @Service:
 *           - Marks this class as a Spring-managed bean
 *           - Specialization of @Component for service layer
 *           - Spring will create ONE instance (singleton by default) and manage
 *           it
 *           - Allows this class to be injected into other classes
 *           via @Autowired
 * 
 * @Slf4j:
 *         - Lombok annotation that generates a logger
 *         - Creates: private static final Logger log =
 *         LoggerFactory.getLogger(UserServiceImpl.class);
 *         - We can then use: log.info("message"), log.debug("message"),
 *         log.error("message")
 * 
 * @Transactional:
 *                 - Methods run inside a database transaction
 *                 - If an exception occurs, all changes are rolled back (ACID
 *                 compliance)
 *                 - On success, changes are committed
 *                 - Critical for data integrity!
 */
@Service // ← Registers this class as a Spring bean in the service layer
@Slf4j // ← Lombok: generates a logger named 'log'
@Transactional // ← All public methods run in a database transaction
public class UserServiceImpl implements UserService {

    /**
     * =====================================================
     * DEPENDENCY INJECTION
     * =====================================================
     * 
     * This is CONSTRUCTOR INJECTION - the recommended way to inject dependencies.
     * 
     * Why constructor injection?
     * 1. Dependencies are required - can't create service without them
     * 2. Immutable - dependencies can be final
     * 3. Testable - easy to pass mocks in tests
     * 4. Clear dependencies - constructor shows what's needed
     * 
     * Spring will:
     * 1. Look for a bean of type UserRepository
     * 2. Create our UserServiceImpl passing that bean to the constructor
     * 3. We never call 'new UserRepository()' ourselves!
     * 
     * Without Spring (manual wiring):
     * EntityManager em =
     * Persistence.createEntityManagerFactory("myPU").createEntityManager();
     * UserRepository repo = new UserRepositoryImpl(em);
     * UserService service = new UserServiceImpl(repo);
     * 
     * With Spring, we just declare what we need and Spring provides it!
     */
    private final UserRepository userRepository;

    /**
     * =====================================================
     * PASSWORD ENCODER
     * =====================================================
     * 
     * We inject the PasswordEncoder to hash passwords before storing.
     * The actual implementation (BCryptPasswordEncoder) is defined in
     * SecurityConfig.
     * 
     * This is the Dependency Inversion Principle in action:
     * - We depend on the abstraction (PasswordEncoder interface)
     * - Not the concrete implementation (BCryptPasswordEncoder)
     * - Easy to swap implementations or mock in tests
     */
    private final PasswordEncoder passwordEncoder;

    // Constructor injection - both dependencies are automatically injected by
    // Spring
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * =====================================================
     * CREATE USER
     * =====================================================
     */
    @Override
    public User createUser(User user) {
        // Log the operation (will show in console at DEBUG level)
        log.info("Creating new user with email: {}", user.getEmail());

        // Business rule: Email must be unique
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Attempted to create user with existing email: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // =====================================================
        // HASH THE PASSWORD BEFORE STORING
        // =====================================================
        //
        // The password comes in as plain text from the request.
        // We MUST hash it before saving to the database!
        //
        // passwordEncoder.encode() does:
        // "MyPassword123" → "$2a$10$N9qo8uLOickgx2ZMRZoMye..."
        //
        // This hash is:
        // - One-way (can't be reversed)
        // - Salted (same password = different hash each time)
        // - Slow (prevents brute force attacks)
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        log.debug("Password hashed successfully for user: {}", user.getEmail());

        // Save the user to the database
        // JPA will:
        // 1. Generate an ID (via IDENTITY strategy)
        // 2. Execute INSERT SQL
        // 3. Fill in the generated ID on the returned entity
        User savedUser = userRepository.save(user);

        log.info("Successfully created user with ID: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * =====================================================
     * FIND USER BY ID
     * =====================================================
     * 
     * @Transactional(readOnly = true) tells Spring:
     *                         - This method only reads data
     *                         - Can optimize (no need to track changes)
     *                         - Some databases can use read replicas
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    /**
     * =====================================================
     * FIND USER BY EMAIL
     * =====================================================
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * =====================================================
     * FIND ALL USERS
     * =====================================================
     * 
     * ⚠️ WARNING: In production, always use pagination!
     * Loading all users at once could crash your app if there are millions.
     * We'll add pagination in the next tasks.
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        log.debug("Finding all users");
        List<User> users = userRepository.findAll();
        log.debug("Found {} users", users.size());
        return users;
    }

    /**
     * =====================================================
     * UPDATE USER
     * =====================================================
     * 
     * The update strategy:
     * 1. Find the existing user (throw exception if not found)
     * 2. Update only the fields that should change
     * 3. Save the updated entity
     * 
     * JPA is smart: if you call save() on an entity with an existing ID,
     * it does an UPDATE, not an INSERT.
     */
    @Override
    public User updateUser(Long id, User updatedUser) {
        log.info("Updating user with ID: {}", id);

        // Find existing user or throw exception
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new RuntimeException("User not found with id: " + id);
                });

        // Check if new email conflicts with another user
        if (!existingUser.getEmail().equals(updatedUser.getEmail())
                && userRepository.existsByEmail(updatedUser.getEmail())) {
            log.warn("Cannot update user - email already in use: {}", updatedUser.getEmail());
            throw new IllegalArgumentException("Email already exists: " + updatedUser.getEmail());
        }

        // Update fields
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        // Note: We DON'T update password here - that should be a separate secured
        // operation
        // Note: We DON'T update role here - that's an admin-only operation

        // Save and return
        // The @PreUpdate in User entity will automatically update 'updatedAt'
        User savedUser = userRepository.save(existingUser);
        log.info("Successfully updated user with ID: {}", id);

        return savedUser;
    }

    /**
     * =====================================================
     * DELETE USER
     * =====================================================
     * 
     * Deletes a user from the database.
     * In production, consider "soft delete" instead (set a 'deleted' flag).
     */
    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        // Check if user exists first
        if (!userRepository.existsById(id)) {
            log.warn("Cannot delete - user not found with ID: {}", id);
            throw new RuntimeException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        log.info("Successfully deleted user with ID: {}", id);
    }

    /**
     * =====================================================
     * EMAIL EXISTS
     * =====================================================
     */
    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
