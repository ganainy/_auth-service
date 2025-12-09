package com.ganainy.authservice.model.entity;

// JPA (Java Persistence API) annotations for mapping Java classes to database tables
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

// Validation annotations (JSR-380) - will be checked before saving to database
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lombok annotations - generate boilerplate code at compile time
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Java time API for timestamps
import java.time.LocalDateTime;

/**
 * User Entity - Maps to the 'users' table in the database.
 * 
 * =====================================================
 * WHAT IS AN ENTITY?
 * =====================================================
 * In JPA (Java Persistence API), an Entity is a Java class that represents
 * a table in your database. Each instance of the entity class represents
 * one row in that table. Each field in the class represents a column.
 * 
 * Think of it as:
 * Java Class → Database Table
 * Object → Row
 * Field → Column
 * 
 * Without JPA, you'd write raw SQL:
 * INSERT INTO users (email, password, first_name) VALUES ('john@example.com',
 * 'hash123', 'John');
 * 
 * With JPA, you just do:
 * User user = new User();
 * user.setEmail("john@example.com");
 * userRepository.save(user); // JPA generates the SQL for you!
 */
@Entity // ← Marks this class as a JPA entity (database table)
@Table(name = "users") // ← Specifies the actual table name in the database
@Data // ← Lombok: Generates getters, setters, toString(), equals(), hashCode()
@Builder // ← Lombok: Generates a builder pattern (User.builder().email("...").build())
@NoArgsConstructor // ← Lombok: Generates a no-argument constructor (required by JPA)
@AllArgsConstructor // ← Lombok: Generates a constructor with all fields
public class User {

    /**
     * =====================================================
     * PRIMARY KEY - @Id and @GeneratedValue
     * =====================================================
     * 
     * @Id marks this field as the primary key (unique identifier for each row).
     * 
     * @GeneratedValue tells JPA HOW to generate the ID:
     *                 - IDENTITY: Database auto-increments (PostgreSQL SERIAL,
     *                 MySQL AUTO_INCREMENT)
     *                 - SEQUENCE: Uses a database sequence (more control,
     *                 PostgreSQL preferred)
     *                 - AUTO: Let Hibernate choose the strategy
     *                 - TABLE: Uses a special table to track IDs (rarely used)
     * 
     *                 We use IDENTITY for simplicity. The database will
     *                 auto-generate IDs:
     *                 1, 2, 3, 4, 5...
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * =====================================================
     * EMAIL FIELD - @Column, @NotBlank, @Email
     * =====================================================
     * 
     * @Column configures how this field maps to the database column:
     *         - name: Column name in the database (default is the field name)
     *         - nullable: Can this column be NULL? (default true)
     *         - unique: Must all values be unique? (creates a unique index)
     *         - length: Maximum length for String columns (default 255)
     * 
     * @NotBlank is a validation annotation:
     *           - Checks that the value is not null
     *           - Checks that the value is not empty ("")
     *           - Checks that the value is not just whitespace (" ")
     *           - The message appears when validation fails
     * 
     * @Email validates that the string looks like an email address.
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /**
     * =====================================================
     * PASSWORD FIELD
     * =====================================================
     * 
     * ⚠️ SECURITY NOTE: This will store the HASHED password, never plain text!
     * When we implement Spring Security, we'll use BCryptPasswordEncoder to hash
     * passwords before saving them.
     * 
     * What gets stored: "$2a$10$N9qo8uLOickgx2ZMRZoMye..." (BCrypt hash)
     * What we NEVER store: "MyPassword123"
     * 
     * @Size validates the length of the value before it's hashed.
     */
    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * =====================================================
     * NAME FIELDS
     * =====================================================
     */
    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    /**
     * =====================================================
     * ROLE FIELD - @Enumerated
     * =====================================================
     * 
     * @Enumerated tells JPA how to store an enum in the database:
     *             - ORDINAL: Stores the position (0, 1, 2...) - DANGEROUS if enum
     *             order changes!
     *             - STRING: Stores the name ("ADMIN", "DOCTOR") - SAFE and readable
     * 
     *             Always use EnumType.STRING to avoid bugs when you add/reorder
     *             enum values.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default // ← Tells Lombok's @Builder to use this default value
    private Role role = Role.PATIENT; // Default role for new users

    /**
     * =====================================================
     * ACCOUNT STATUS FLAGS
     * =====================================================
     * 
     * These are standard fields for account management:
     * - enabled: Can the user log in?
     * - accountNonExpired: Has the account expired?
     * - credentialsNonExpired: Has the password expired?
     * - accountNonLocked: Is the account locked (e.g., too many failed logins)?
     * 
     * These will be used by Spring Security's UserDetails interface.
     */
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "account_non_expired", nullable = false)
    @Builder.Default
    private boolean accountNonExpired = true;

    @Column(name = "credentials_non_expired", nullable = false)
    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Column(name = "account_non_locked", nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    /**
     * =====================================================
     * AUDIT FIELDS - @PrePersist and @PreUpdate
     * =====================================================
     * 
     * Audit fields track WHEN data was created and modified.
     * This is critical for:
     * - Debugging ("When was this user created?")
     * - Security ("When was the last login?")
     * - Compliance (HIPAA requires audit trails)
     * 
     * @Column(updatable = false) means this column won't be changed after insert.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * =====================================================
     * JPA LIFECYCLE CALLBACKS
     * =====================================================
     * 
     * These methods are automatically called by JPA:
     * 
     * @PrePersist: Called BEFORE the entity is inserted into the database.
     *              Perfect for setting "createdAt" timestamps.
     * 
     * @PreUpdate: Called BEFORE the entity is updated in the database.
     *             Perfect for updating "updatedAt" timestamps.
     * 
     *             Without Spring, you'd have to remember to set these manually:
     *             user.setCreatedAt(LocalDateTime.now());
     *             user.setUpdatedAt(LocalDateTime.now());
     *             entityManager.persist(user);
     * 
     *             With @PrePersist/@PreUpdate, it's automatic!
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * =====================================================
     * INNER ENUM: Role
     * =====================================================
     * 
     * Defines the possible roles in our healthcare system.
     * We'll expand this with proper authorization in Week 2.
     */
    public enum Role {
        ADMIN, // System administrator
        DOCTOR, // Medical doctor
        NURSE, // Nursing staff
        PATIENT, // Patient
        RECEPTIONIST // Front desk staff
    }
}
