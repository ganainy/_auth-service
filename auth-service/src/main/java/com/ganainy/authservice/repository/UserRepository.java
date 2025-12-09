package com.ganainy.authservice.repository;

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
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * =====================================================
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
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an email already exists in the database.
     * 
     * Useful for registration - we want to reject duplicate emails
     * before trying to save (which would throw a constraint violation).
     * 
     * Generated SQL: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);

}
