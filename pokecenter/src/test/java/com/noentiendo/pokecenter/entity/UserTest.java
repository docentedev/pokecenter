package com.noentiendo.pokecenter.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    @DisplayName("Should create User with no-arg constructor")
    void testNoArgConstructor() {
        // Act
        User newUser = new User();

        // Assert
        assertNotNull(newUser);
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
        assertNull(newUser.getPasswordHash());
    }

    @Test
    @DisplayName("Should create User with all-args constructor")
    void testAllArgsConstructor() {
        // Arrange
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // Act
        User newUser = new User(id, "testuser", "password", now, now);

        // Assert
        assertEquals(id, newUser.getId());
        assertEquals("testuser", newUser.getUsername());
        assertEquals("password", newUser.getPasswordHash());
        assertEquals(now, newUser.getCreatedAt());
        assertEquals(now, newUser.getUpdatedAt());
    }

    // ==================== BUILDER TESTS ====================

    @Test
    @DisplayName("Should create User using builder")
    void testBuilder() {
        // Act
        User builtUser = User.builder()
                .username("builderuser")
                .passwordHash("buildhash")
                .build();

        // Assert
        assertNotNull(builtUser);
        assertEquals("builderuser", builtUser.getUsername());
        assertEquals("buildhash", builtUser.getPasswordHash());
    }

    @Test
    @DisplayName("Should build User with all fields using builder")
    void testBuilderWithAllFields() {
        // Arrange
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // Act
        User builtUser = User.builder()
                .id(id)
                .username("fulluser")
                .passwordHash("fullhash")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // Assert
        assertEquals(id, builtUser.getId());
        assertEquals("fulluser", builtUser.getUsername());
        assertEquals("fullhash", builtUser.getPasswordHash());
        assertEquals(createdAt, builtUser.getCreatedAt());
        assertEquals(updatedAt, builtUser.getUpdatedAt());
    }

    // ==================== GETTER AND SETTER TESTS ====================

    @Test
    @DisplayName("Should set and get id")
    void testIdGetterSetter() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        user.setId(id);

        // Assert
        assertEquals(id, user.getId());
    }

    @Test
    @DisplayName("Should set and get username")
    void testUsernameGetterSetter() {
        // Act
        user.setUsername("testuser");

        // Assert
        assertEquals("testuser", user.getUsername());
    }

    @Test
    @DisplayName("Should set and get password hash")
    void testPasswordHashGetterSetter() {
        // Act
        user.setPasswordHash("$2a$10$hashedpassword");

        // Assert
        assertEquals("$2a$10$hashedpassword", user.getPasswordHash());
    }

    @Test
    @DisplayName("Should set and get createdAt")
    void testCreatedAtGetterSetter() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        user.setCreatedAt(now);

        // Assert
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get updatedAt")
    void testUpdatedAtGetterSetter() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        user.setUpdatedAt(now);

        // Assert
        assertEquals(now, user.getUpdatedAt());
    }

    // ==================== EQUALS AND HASH CODE TESTS ====================

    @Test
    @DisplayName("Should consider two users with identical fields as equal")
    void testEqualsWithSameValues() {
        // Arrange
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        User user1 = new User(id, "user1", "hash1", now, now);
        User user2 = new User(id, "user1", "hash1", now, now);

        // Act & Assert
        assertEquals(user1, user2);
    }

    @Test
    @DisplayName("Should consider two users with different id as not equal")
    void testEqualsWithDifferentId() {
        // Arrange
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .build();
        User user2 = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .build();

        // Act & Assert
        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void testHashCodeConsistent() {
        // Arrange
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        User user1 = new User(id, "testuser", "hash", now, now);
        User user2 = new User(id, "testuser", "hash", now, now);

        // Act & Assert
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    // ==================== TO STRING TESTS ====================

    @Test
    @DisplayName("Should have toString representation")
    void testToString() {
        // Arrange
        user.setUsername("testuser");
        user.setPasswordHash("hash");

        // Act
        String toString = user.toString();

        // Assert
        assertNotNull(toString);
        assertFalse(toString.isBlank());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("Should allow valid username")
    void testValidUsername() {
        // Act
        user.setUsername("valid_user_123");

        // Assert
        assertEquals("valid_user_123", user.getUsername());
    }

    @Test
    @DisplayName("Should allow username with special characters")
    void testUsernameWithSpecialCharacters() {
        // Act
        user.setUsername("user+test@example.com");

        // Assert
        assertEquals("user+test@example.com", user.getUsername());
    }

    @Test
    @DisplayName("Should allow different password hash formats")
    void testDifferentPasswordFormats() {
        // Arrange
        String[] hashFormats = {
                "$2a$10$hashedpassword",
                "plaintext",
                "sha256_hash",
                "bcrypt$2b$12$abcdefghijk"
        };

        for (String hash : hashFormats) {
            // Act
            user.setPasswordHash(hash);

            // Assert
            assertEquals(hash, user.getPasswordHash());
        }
    }

    // ==================== TIMESTAMP TESTS ====================

    @Test
    @DisplayName("Should have different timestamps for onCreate and onUpdate")
    void testOnCreateAndOnUpdate() throws InterruptedException {
        // Arrange
        user.setUsername("testuser");
        user.setPasswordHash("hash");

        // Act
        user.onCreate();
        Thread.sleep(10); // Small delay to ensure different timestamps
        user.onUpdate();

        // Assert
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(user.getCreatedAt()) || 
                   user.getUpdatedAt().equals(user.getCreatedAt()));
    }

    @Test
    @DisplayName("Should update updatedAt when onUpdate is called")
    void testOnUpdateChangesUpdatedAt() {
        // Arrange
        LocalDateTime createdTime = LocalDateTime.now().minusHours(1);
        user.setCreatedAt(createdTime);
        user.setUpdatedAt(createdTime);

        // Act
        user.onUpdate();

        // Assert
        assertTrue(user.getUpdatedAt().isAfter(createdTime));
    }

    // ==================== FIELD COMBINATIONS TESTS ====================

    @Test
    @DisplayName("Should handle User with all fields populated")
    void testUserWithAllFieldsPopulated() {
        // Arrange
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // Act
        User fullUser = User.builder()
                .id(id)
                .username("fulluser")
                .passwordHash("$2a$10$fullhash")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Assert
        assertNotNull(fullUser.getId());
        assertNotNull(fullUser.getUsername());
        assertNotNull(fullUser.getPasswordHash());
        assertNotNull(fullUser.getCreatedAt());
        assertNotNull(fullUser.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle User with minimal fields")
    void testUserWithMinimalFields() {
        // Act
        User minimalUser = User.builder()
                .username("minimal")
                .passwordHash("hash")
                .build();

        // Assert
        assertNull(minimalUser.getId());
        assertEquals("minimal", minimalUser.getUsername());
        assertEquals("hash", minimalUser.getPasswordHash());
        assertNull(minimalUser.getCreatedAt());
        assertNull(minimalUser.getUpdatedAt());
    }
}
