package com.ganainy.authservice.repository;

// Spring Data JPA imports
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// Our entity
import com.ganainy.authservice.model.entity.User;

// Java imports
import java.util.List;
import java.util.Optional;

/**
 * UserRepository - Data Access Layer for User entity.
 * 
 * =====================================================
 * WHAT IS A SPRING DATA JPA REPOSITORY?
 * =====================================================
 * 
 * A Repository is an interface that tells Spring:
 * "I want to do database operations on this entity."
 * 
 * The MAGIC: You write ZERO implementation code!
 * Spring generates all the SQL queries at runtime.
 * 
 * By extending JpaRepository<User, Long>, you get 20+ methods FREE:
 * 
 * CRUD Operations (inherited):
 * - save(User user) → INSERT or UPDATE (auto-detects!)
 * - findById(Long id) → SELECT * FROM users WHERE id = ?
 * - findAll() → SELECT * FROM users
 * - delete(User user) → DELETE FROM users WHERE id = ?
 * - deleteById(Long id) → DELETE FROM users WHERE id = ?
 * - count() → SELECT COUNT(*) FROM users
 * - existsById(Long id) → SELECT EXISTS(...)
 * 
 * Pagination & Sorting (inherited):
 * - findAll(Pageable pageable) → SELECT ... LIMIT ? OFFSET ?
 * - findAll(Sort sort) → SELECT ... ORDER BY ?
 * 
 * =====================================================
 * HOW DOES SPRING KNOW WHICH TABLE TO USE?
 * =====================================================
 * 
 * Look at JpaRepository<User, Long>:
 * - User = The entity type (maps to "users" table via @Table)
 * - Long = The type of the primary key (@Id field in User)
 * 
 * Spring reads the @Entity and @Table annotations from User.java
 * to know the table name and column mappings.
 * 
 * =====================================================
 * WHAT IS @Repository?
 * =====================================================
 * 
 * @Repository is a Spring stereotype annotation that:
 *             1. Marks this as a Spring-managed bean (like @Component)
 *             2. Enables exception translation (converts SQL exceptions to
 *             Spring's DataAccessException hierarchy)
 * 
 *             Note: For JpaRepository, @Repository is technically optional
 *             because
 *             Spring Data automatically detects interfaces extending
 *             JpaRepository.
 *             But it's good practice to include it for clarity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * =====================================================
     * DERIVED QUERY METHODS - The Real Magic!
     * =====================================================
     * 
     * Spring Data JPA can generate queries just from the METHOD NAME!
     * 
     * How it works:
     * findByEmail → find + By + Email
     * - find = SELECT query
     * - By = WHERE clause starts
     * - Email = column name (from User.email field)
     * 
     * Generated SQL: SELECT * FROM users WHERE email = ?
     * 
     * Why Optional<User>?
     * - The user might not exist (returns Optional.empty())
     * - Forces you to handle the "not found" case explicitly
     * - Avoids null pointer exceptions!
     * 
     * Example usage:
     * Optional<User> user = userRepository.findByEmail("john@example.com");
     * if (user.isPresent()) {
     * System.out.println("Found: " + user.get().getFirstName());
     * } else {
     * System.out.println("User not found");
     * }
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists by email.
     * 
     * Derived query: exists + By + Email
     * Generated SQL: SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
     * 
     * Why use this?
     * - More efficient than findByEmail when you just need to check existence
     * - Returns a simple boolean instead of loading the entire entity
     * - Perfect for registration: "Is this email already taken?"
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role.
     * 
     * Derived query: find + All + By + Role
     * Generated SQL: SELECT * FROM users WHERE role = ?
     * 
     * Use case: "List all doctors in our system"
     */
    List<User> findAllByRole(User.Role role);

    /**
     * Find all enabled users with a specific role.
     * 
     * Multiple conditions combined with "And":
     * find + All + By + Role + And + EnabledTrue
     * 
     * Generated SQL: SELECT * FROM users WHERE role = ? AND enabled = true
     * 
     * "EnabledTrue" is a special suffix that means "enabled = true"
     * Other options: EnabledFalse, EnabledIsTrue, EnabledIsNull
     */
    List<User> findAllByRoleAndEnabledTrue(User.Role role);

    /**
     * Search users by name (case-insensitive partial match).
     * 
     * "Containing" = SQL LIKE '%value%'
     * "IgnoreCase" = case-insensitive comparison
     * "Or" = combines multiple conditions
     * 
     * Generated SQL:
     * SELECT * FROM users
     * WHERE LOWER(first_name) LIKE LOWER('%?%')
     * OR LOWER(last_name) LIKE LOWER('%?%')
     * 
     * Use case: Search bar for finding users by name
     */
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    /**
     * =====================================================
     * CUSTOM JPQL QUERY - When derived queries aren't enough
     * =====================================================
     * 
     * @Query lets you write custom JPQL (Java Persistence Query Language).
     * 
     *        JPQL vs SQL:
     *        - SQL refers to TABLE names and COLUMN names
     *        - JPQL refers to ENTITY names and FIELD names
     * 
     *        Notice:
     *        - "User u" not "users u" (entity class name, not table name)
     *        - "u.email" not "u.email" (Java field name matches, but that's not
     *        always the case)
     *        - ":email" is a named parameter (bound via @Param)
     * 
     *        @Param("email") binds the method parameter to the :email in the query.
     * 
     *        Why use @Query instead of derived query?
     *        - Complex queries that can't be expressed in method names
     *        - More readable than a very long method name
     *        - Performance optimization (selecting only needed columns)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true AND u.accountNonLocked = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    /**
     * Count all users with a specific role.
     * 
     * Derived query: count + By + Role
     * Generated SQL: SELECT COUNT(*) FROM users WHERE role = ?
     * 
     * Use case: Dashboard showing "50 patients, 10 doctors, 3 admins"
     */
    long countByRole(User.Role role);

    /**
     * Count all enabled users.
     * 
     * Generated SQL: SELECT COUNT(*) FROM users WHERE enabled = true
     */
    long countByEnabledTrue();
}
