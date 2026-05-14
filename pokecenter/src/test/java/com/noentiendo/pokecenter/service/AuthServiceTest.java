package com.noentiendo.pokecenter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.noentiendo.pokecenter.dto.AuthResponse;
import com.noentiendo.pokecenter.dto.LoginRequest;
import com.noentiendo.pokecenter.dto.RegisterRequest;
import com.noentiendo.pokecenter.entity.User;
import com.noentiendo.pokecenter.exception.AuthenticationException;
import com.noentiendo.pokecenter.exception.ValidationException;
import com.noentiendo.pokecenter.repository.UserRepository;
import com.noentiendo.pokecenter.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("testuser", "password123");
        validLoginRequest = new LoginRequest("testuser", "password123");
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .passwordHash("$2a$10$hashedpassword")
                .build();
    }

    // ==================== REGISTER TESTS ====================

    @Test
    @DisplayName("Should successfully register a new user")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token-123");
        when(jwtUtil.getExpiration()).thenReturn(3600000L);

        // Act
        AuthResponse response = authService.register(validRegisterRequest);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.username());
        assertEquals("jwt-token-123", response.token());
        assertEquals(3600000L, response.expiresIn());
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken("testuser");
    }

    @Test
    @DisplayName("Should throw ValidationException when username already exists")
    void testRegisterWithDuplicateUsername() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.register(validRegisterRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when username is empty")
    void testRegisterWithEmptyUsername() {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest("", "password123");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.register(invalidRequest);
        });

        assertEquals("Username cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw ValidationException when username is null")
    void testRegisterWithNullUsername() {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest(null, "password123");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.register(invalidRequest);
        });

        assertEquals("Username cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw ValidationException when password is empty")
    void testRegisterWithEmptyPassword() {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest("testuser", "");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.register(invalidRequest);
        });

        assertEquals("Password cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw ValidationException when password is null")
    void testRegisterWithNullPassword() {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest("testuser", null);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.register(invalidRequest);
        });

        assertEquals("Password cannot be empty", exception.getMessage());
    }

    // ==================== LOGIN TESTS ====================

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLoginSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$hashedpassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser")).thenReturn("jwt-token-456");
        when(jwtUtil.getExpiration()).thenReturn(3600000L);

        // Act
        AuthResponse response = authService.login(validLoginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.username());
        assertEquals("jwt-token-456", response.token());
        assertEquals(3600000L, response.expiresIn());
        verify(passwordEncoder, times(1)).matches("password123", "$2a$10$hashedpassword");
    }

    @Test
    @DisplayName("Should throw AuthenticationException when user not found")
    void testLoginWithNonExistentUser() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(new LoginRequest("nonexistent", "password123"));
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw AuthenticationException when password is incorrect")
    void testLoginWithWrongPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedpassword")).thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(new LoginRequest("testuser", "wrongpassword"));
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should throw ValidationException when login username is empty")
    void testLoginWithEmptyUsername() {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest("", "password123");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.login(invalidRequest);
        });

        assertEquals("Username cannot be empty", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw ValidationException when login password is empty")
    void testLoginWithEmptyPassword() {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest("testuser", "");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.login(invalidRequest);
        });

        assertEquals("Password cannot be empty", exception.getMessage());
    }

    // ==================== VALIDATE TOKEN TESTS ====================

    @Test
    @DisplayName("Should return true for valid token")
    void testValidateValidToken() {
        // Arrange
        String validToken = "valid-jwt-token";
        when(jwtUtil.validateToken(validToken)).thenReturn(true);

        // Act
        boolean result = authService.validateToken(validToken);

        // Assert
        assertTrue(result);
        verify(jwtUtil, times(1)).validateToken(validToken);
    }

    @Test
    @DisplayName("Should return false for invalid token")
    void testValidateInvalidToken() {
        // Arrange
        String invalidToken = "invalid-jwt-token";
        when(jwtUtil.validateToken(invalidToken)).thenReturn(false);

        // Act
        boolean result = authService.validateToken(invalidToken);

        // Assert
        assertFalse(result);
        verify(jwtUtil, times(1)).validateToken(invalidToken);
    }

    @Test
    @DisplayName("Should return false for expired token")
    void testValidateExpiredToken() {
        // Arrange
        String expiredToken = "expired-jwt-token";
        when(jwtUtil.validateToken(expiredToken)).thenReturn(false);

        // Act
        boolean result = authService.validateToken(expiredToken);

        // Assert
        assertFalse(result);
    }
}
