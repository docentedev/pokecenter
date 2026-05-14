package com.noentiendo.pokecenter.repository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.noentiendo.pokecenter.entity.User;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .passwordHash("$2a$10$hashedpassword")
                .build();
    }

    // ==================== FIND BY USERNAME TESTS ====================

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsernameSuccess() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
        assertEquals("$2a$10$hashedpassword", foundUser.get().getPasswordHash());
    }

    @Test
    @DisplayName("Should return empty Optional when user not found")
    void testFindByUsernameNotFound() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Assert
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Should find user with special characters in username")
    void testFindByUsernameWithSpecialCharacters() {
        // Arrange
        User specialUser = User.builder()
                .username("user+test@example.com")
                .passwordHash("hash123")
                .build();
        entityManager.persistAndFlush(specialUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("user+test@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("user+test@example.com", foundUser.get().getUsername());
    }

    @Test
    @DisplayName("Should be case-sensitive when finding by username")
    void testFindByUsernameCaseSensitive() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("TestUser"); // Different case

        // Assert
        assertTrue(foundUser.isEmpty());
    }

    @Test
    @DisplayName("Should find exact match only")
    void testFindByUsernameExactMatch() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser2");

        // Assert
        assertTrue(foundUser.isEmpty());
    }

    // ==================== SAVE AND RETRIEVE TESTS ====================

    @Test
    @DisplayName("Should save user and retrieve by id")
    void testSaveAndRetrieveUser() {
        // Act
        User savedUser = userRepository.save(testUser);
        User retrievedUser = userRepository.findById(savedUser.getId()).orElse(null);

        // Assert
        assertNotNull(retrievedUser);
        assertEquals(testUser.getUsername(), retrievedUser.getUsername());
        assertEquals(testUser.getPasswordHash(), retrievedUser.getPasswordHash());
    }

    @Test
    @DisplayName("Should assign UUID on save")
    void testUUIDAssignedOnSave() {
        // Arrange
        assertNull(testUser.getId());

        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        assertNotNull(savedUser.getId());
        assertTrue(savedUser.getId() instanceof UUID);
    }

    @Test
    @DisplayName("Should persist createdAt and updatedAt timestamps")
    void testTimestampsPersisted() {
        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    // ==================== UNIQUE CONSTRAINT TESTS ====================

    @Test
    @DisplayName("Should enforce username uniqueness constraint")
    void testUsernameUniqueConstraint() {
        // Arrange
        User user1 = User.builder()
                .username("duplicate")
                .passwordHash("hash1")
                .build();
        User user2 = User.builder()
                .username("duplicate")
                .passwordHash("hash2")
                .build();

        userRepository.save(user1);
        entityManager.flush();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
            entityManager.flush();
        });
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Should delete user by id")
    void testDeleteUserById() {
        // Arrange
        User savedUser = userRepository.save(testUser);
        UUID userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        assertTrue(userRepository.findById(userId).isEmpty());
    }

    @Test
    @DisplayName("Should delete user and not find by username after")
    void testDeleteUserAndNotFindByUsername() {
        // Arrange
        userRepository.save(testUser);

        // Act
        userRepository.deleteAll();

        // Assert
        assertTrue(userRepository.findByUsername("testuser").isEmpty());
    }

    // ==================== COUNT TESTS ====================

    @Test
    @DisplayName("Should count users correctly")
    void testCountUsers() {
        // Arrange
        userRepository.save(testUser);
        User user2 = User.builder()
                .username("user2")
                .passwordHash("hash2")
                .build();
        userRepository.save(user2);

        // Act
        long count = userRepository.count();

        // Assert
        assertEquals(2, count);
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @DisplayName("Should update user password hash")
    void testUpdateUserPasswordHash() {
        // Arrange
        User savedUser = userRepository.save(testUser);
        String newPasswordHash = "$2a$10$newhashedpassword";

        // Act
        savedUser.setPasswordHash(newPasswordHash);
        User updatedUser = userRepository.save(savedUser);

        // Assert
        assertEquals(newPasswordHash, updatedUser.getPasswordHash());
    }

    @Test
    @DisplayName("Should update user and persist changes")
    void testUpdateAndPersistUser() {
        // Arrange
        User savedUser = userRepository.save(testUser);
        long initialId = savedUser.getId().getMostSignificantBits();

        // Act
        savedUser.setPasswordHash("newpassword");
        userRepository.saveAndFlush(savedUser);
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());

        // Assert
        assertTrue(retrievedUser.isPresent());
        assertEquals("newpassword", retrievedUser.get().getPasswordHash());
    }

    // ==================== MULTIPLE USERS TESTS ====================

    @Test
    @DisplayName("Should retrieve all users")
    void testFindAllUsers() {
        // Arrange
        userRepository.save(testUser);
        User user2 = User.builder()
                .username("user2")
                .passwordHash("hash2")
                .build();
        userRepository.save(user2);

        // Act
        var allUsers = userRepository.findAll();

        // Assert
        assertEquals(2, allUsers.size());
    }

    @Test
    @DisplayName("Should handle multiple findByUsername calls")
    void testMultipleFindByUsernameCalls() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> result1 = userRepository.findByUsername("testuser");
        Optional<User> result2 = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(result1.get().getId(), result2.get().getId());
    }
}
