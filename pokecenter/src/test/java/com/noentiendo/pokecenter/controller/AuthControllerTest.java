package com.noentiendo.pokecenter.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noentiendo.pokecenter.dto.AuthResponse;
import com.noentiendo.pokecenter.dto.LoginRequest;
import com.noentiendo.pokecenter.dto.RegisterRequest;
import com.noentiendo.pokecenter.exception.AuthenticationException;
import com.noentiendo.pokecenter.exception.GlobalExceptionHandler;
import com.noentiendo.pokecenter.exception.ValidationException;
import com.noentiendo.pokecenter.service.AuthService;
import com.noentiendo.pokecenter.util.JwtUtil;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @MockitoBean
    private AuthService authService;

        @MockitoBean
        private JwtUtil jwtUtil;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("testuser", "password123");
        validLoginRequest = new LoginRequest("testuser", "password123");
        authResponse = new AuthResponse("jwt-token-123", 3600000L, "testuser");
    }

    // ==================== REGISTER ENDPOINT TESTS ====================

    @Test
    @DisplayName("POST /api/auth/register - Should register new user and return 201")
    void testRegisterSuccess() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", equalTo("testuser")))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.expiresIn", is(3600000)));

        verify(authService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for duplicate username")
    void testRegisterDuplicateUsername() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ValidationException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.error.message", equalTo("Username already exists")));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for empty username")
    void testRegisterEmptyUsername() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest("", "password123");
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ValidationException("Username cannot be empty"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for empty password")
    void testRegisterEmptyPassword() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest("testuser", "");
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ValidationException("Password cannot be empty"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== LOGIN ENDPOINT TESTS ====================

    @Test
    @DisplayName("POST /api/auth/login - Should login and return 200 with token")
    void testLoginSuccess() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", equalTo("testuser")))
                .andExpect(jsonPath("$.data.token", equalTo("jwt-token-123")))
                .andExpect(jsonPath("$.data.expiresIn", is(3600000)));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 for invalid credentials")
    void testLoginInvalidCredentials() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("AUTHENTICATION_ERROR")))
                .andExpect(jsonPath("$.error.message", equalTo("Invalid username or password")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 401 for wrong password")
    void testLoginWrongPassword() throws Exception {
        // Arrange
        LoginRequest wrongPasswordRequest = new LoginRequest("testuser", "wrongpassword");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 400 for empty username")
    void testLoginEmptyUsername() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest("", "password123");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ValidationException("Username cannot be empty"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== VALIDATE ENDPOINT TESTS ====================

    @Test
    @DisplayName("GET /api/auth/validate - Should return true for valid token")
    void testValidateValidToken() throws Exception {
        // Arrange
        when(authService.validateToken("valid-token")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(true)));

        verify(authService, times(1)).validateToken("valid-token");
    }

    @Test
    @DisplayName("GET /api/auth/validate - Should return false for invalid token")
    void testValidateInvalidToken() throws Exception {
        // Arrange
        when(authService.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(false)));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Should return 400 for missing Authorization header")
    void testValidateMissingHeader() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Should return 400 for missing Bearer prefix")
    void testValidateMissingBearerPrefix() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.error.message", 
                        equalTo("Missing or invalid Authorization header")));
    }

    @Test
    @DisplayName("GET /api/auth/validate - Should return 400 for empty Bearer token")
    void testValidateEmptyBearerToken() throws Exception {
        when(authService.validateToken("")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(false)));
    }

    // ==================== RESPONSE FORMAT TESTS ====================

    @Test
    @DisplayName("Register response should have correct ApiResponse structure")
    void testRegisterResponseStructure() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.expiresIn").exists())
                .andExpect(jsonPath("$.data.username").exists());
    }

    @Test
    @DisplayName("Error response should have correct ApiResponse structure")
    void testErrorResponseStructure() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.code").exists())
                .andExpect(jsonPath("$.error.message").exists());
    }
}
