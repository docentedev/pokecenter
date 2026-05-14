package com.noentiendo.pokecenter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TEST_USERNAME = "testuser";

    // ==================== GENERATE TOKEN TESTS ====================

    @Test
    @DisplayName("Should generate a valid JWT token")
    void testGenerateTokenSuccess() {
        // Act
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // Assert
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(token.contains("."));
        assertEquals(3, token.split("\\.").length); // JWT format: header.payload.signature
    }

    @Test
    @DisplayName("Should generate valid tokens for repeated calls")
    void testGenerateTokenRepeatedCalls() {
        // Act
        String token1 = jwtUtil.generateToken(TEST_USERNAME);
        String token2 = jwtUtil.generateToken(TEST_USERNAME);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertTrue(jwtUtil.validateToken(token1));
        assertTrue(jwtUtil.validateToken(token2));
        assertEquals(TEST_USERNAME, jwtUtil.getUsername(token1));
        assertEquals(TEST_USERNAME, jwtUtil.getUsername(token2));
    }

    @Test
    @DisplayName("Should generate different tokens for different usernames")
    void testGenerateTokenDifferentUsers() {
        // Act
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    // ==================== VALIDATE TOKEN TESTS ====================

    @Test
    @DisplayName("Should validate a freshly generated token")
    void testValidateValidToken() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalid token format")
    void testValidateInvalidTokenFormat() {
        // Act
        boolean isValid = jwtUtil.validateToken("invalid.token.format");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject malformed token")
    void testValidateMalformedToken() {
        // Act
        boolean isValid = jwtUtil.validateToken("this-is-not-a-jwt");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject empty token")
    void testValidateEmptyToken() {
        // Act
        boolean isValid = jwtUtil.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject null token")
    void testValidateNullToken() {
        // Act
        boolean isValid = jwtUtil.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should reject tampered token")
    void testValidateTamperedToken() {
        // Arrange
        String validToken = jwtUtil.generateToken(TEST_USERNAME);
        // Tamper with the token by changing one character
        String tamperedToken = validToken.substring(0, validToken.length() - 1) + "X";

        // Act
        boolean isValid = jwtUtil.validateToken(tamperedToken);

        // Assert
        assertFalse(isValid);
    }

    // ==================== GET USERNAME TESTS ====================

    @Test
    @DisplayName("Should extract username from valid token")
    void testGetUsernameFromValidToken() {
        // Arrange
        String token = jwtUtil.generateToken(TEST_USERNAME);

        // Act
        String extractedUsername = jwtUtil.getUsername(token);

        // Assert
        assertNotNull(extractedUsername);
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    @DisplayName("Should return null for invalid token")
    void testGetUsernameFromInvalidToken() {
        // Act
        String username = jwtUtil.getUsername("invalid-token");

        // Assert
        assertNull(username);
    }

    @Test
    @DisplayName("Should return null for malformed token")
    void testGetUsernameFromMalformedToken() {
        // Act
        String username = jwtUtil.getUsername("not.a.jwt");

        // Assert
        assertNull(username);
    }

    @Test
    @DisplayName("Should return null for empty token")
    void testGetUsernameFromEmptyToken() {
        // Act
        String username = jwtUtil.getUsername("");

        // Assert
        assertNull(username);
    }

    @Test
    @DisplayName("Should extract correct username from different users")
    void testGetUsernameMultipleUsers() {
        // Arrange
        String[] usernames = {"alice", "bob", "charlie", "user@example.com"};

        for (String username : usernames) {
            // Act
            String token = jwtUtil.generateToken(username);
            String extractedUsername = jwtUtil.getUsername(token);

            // Assert
            assertEquals(username, extractedUsername);
        }
    }

    // ==================== EXPIRATION TESTS ====================

    @Test
    @DisplayName("Should return expiration time")
    void testGetExpiration() {
        // Act
        long expiration = jwtUtil.getExpiration();

        // Assert
        assertTrue(expiration > 0);
        assertEquals(3600000, expiration); // Default is 1 hour
    }

    @Test
    @DisplayName("Expiration should be consistent")
    void testExpirationConsistent() {
        // Act
        long expiration1 = jwtUtil.getExpiration();
        long expiration2 = jwtUtil.getExpiration();

        // Assert
        assertEquals(expiration1, expiration2);
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Complete flow: generate, validate, and extract username")
    void testCompleteJwtFlow() {
        // Arrange
        String username = "integration_test_user";

        // Act
        String token = jwtUtil.generateToken(username);
        boolean isValid = jwtUtil.validateToken(token);
        String extractedUsername = jwtUtil.getUsername(token);

        // Assert
        assertTrue(isValid);
        assertEquals(username, extractedUsername);
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("Token with special characters in username")
    void testTokenWithSpecialCharactersUsername() {
        // Arrange
        String username = "user+test@example.com";

        // Act
        String token = jwtUtil.generateToken(username);
        boolean isValid = jwtUtil.validateToken(token);
        String extractedUsername = jwtUtil.getUsername(token);

        // Assert
        assertTrue(isValid);
        assertEquals(username, extractedUsername);
    }
}
