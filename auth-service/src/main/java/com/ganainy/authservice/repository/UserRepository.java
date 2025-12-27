package com.ganainy.authservice.repository;

<<<<<<< HEAD
// JPA repository interface - provides CRUD operations for free
import org.springframework.data.jpa.repository.JpaRepository;
// Indicates this is a repository bean (optional but good for documentation)
import org.springframework.stereotype.Repository;

// Our User entity
import com.ganainy.authservice.model.entity.User;

// Java Optional - modern way to handle "might be null" values
import java.util.Optional;

/**
 * UserRepository - Interface for database operations on the User entity.
 * 
 * =====================================================
 * WHAT IS A REPOSITORY IN SPRING DATA JPA?
 * =====================================================
 * 
 * A Repository is an interface that provides database access methods.
 * The magic of Spring Data JPA is that YOU DON'T WRITE IMPLEMENTATIONS!
 * Spring automatically generates the implementation at runtime.
 * 
 * Plain Java equivalent (what you'd have to write without Spring):
 * 
 * public class UserDao {
 * private EntityManager em;
 * 
 * public User save(User user) {
 * em.persist(user);
 * return user;
 * }
 * 
 * public User findById(Long id) {
 * return em.find(User.class, id);
 * }
 * 
 * public List<User> findAll() {
 * return em.createQuery("SELECT u FROM User u", User.class).getResultList();
 * }
 * 
 * public void delete(User user) {
 * em.remove(user);
 * }
 * }
 * 
 * With Spring Data JPA, you just extend JpaRepository and get ALL of this for
 * free!
 * 
 * =====================================================
 * WHAT DOES JpaRepository<User, Long> MEAN?
 * =====================================================
 * 
 * JpaRepository is a generic interface that takes two type parameters:
 * 1. User - The entity type this repository manages
 * 2. Long - The type of the entity's primary key (@Id field)
 * 
 * By extending JpaRepository<User, Long>, we automatically get these methods:
 * 
 * CRUD Methods (inherited from CrudRepository):
 * ├── save(User entity) → Save a user (insert or update)
 * ├── saveAll(Iterable<User>) → Save multiple users
 * ├── findById(Long id) → Find user by ID (returns Optional<User>)
 * ├── existsById(Long id) → Check if user exists
 * ├── findAll() → Get all users
 * ├── findAllById(Iterable<Long>) → Get users by multiple IDs
 * ├── count() → Count total users
 * ├── deleteById(Long id) → Delete user by ID
 * ├── delete(User entity) → Delete a specific user
 * └── deleteAll() → Delete all users
 * 
 * Paging & Sorting Methods (from PagingAndSortingRepository):
 * ├── findAll(Sort sort) → Get all users sorted
 * └── findAll(Pageable pageable) → Get users with pagination
 * 
 * JPA-Specific Methods (from JpaRepository):
 * ├── flush() → Flush pending changes to DB
 * ├── saveAndFlush(User entity) → Save and immediately flush
 * └── deleteInBatch(Iterable) → Batch delete for performance
 * 
 * That's about 20+ methods WITHOUT writing any code!
 */
@Repository // Marks this as a Spring bean. Spring will create an instance and manage it.
            // Also translates database exceptions to Spring's DataAccessException
            // hierarchy.
=======
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
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * =====================================================
<<<<<<< HEAD
     * QUERY METHODS - Derived from Method Name
     * =====================================================
     * 
     * Spring Data JPA can create queries based on method names!
     * 
     * The pattern is: findBy + FieldName + Condition
     * 
     * Examples:
     * findByEmail(String email) → WHERE email = ?
     * findByFirstName(String name) → WHERE first_name = ?
     * findByRoleAndEnabled(...) → WHERE role = ? AND enabled = ?
     * findByEmailContaining(String) → WHERE email LIKE '%?%'
     * findByCreatedAtAfter(LocalDateTime) → WHERE created_at > ?
     * 
     * Common keywords:
     * And, Or, Between, LessThan, GreaterThan, After, Before,
     * IsNull, IsNotNull, Like, NotLike, StartingWith, EndingWith,
     * Containing, OrderBy, Not, In, NotIn, True, False, IgnoreCase
     * 
     * The method returns Optional<User> because:
     * - A user with this email might not exist
     * - Optional forces us to handle the "not found" case explicitly
     * - Better than returning null which could cause NullPointerException
=======
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
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
     */
    Optional<User> findByEmail(String email);

    /**
<<<<<<< HEAD
     * Check if an email already exists in the database.
     * 
     * Useful for registration - we want to reject duplicate emails
     * before trying to save (which would throw a constraint violation).
     * 
     * Generated SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);

=======
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
>>>>>>> c91ebb48e3e199215cab9f2b70c7fb1847f4e963
}
