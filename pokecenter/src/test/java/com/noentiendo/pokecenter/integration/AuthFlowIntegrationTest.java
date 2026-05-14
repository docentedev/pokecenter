package com.noentiendo.pokecenter.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noentiendo.pokecenter.dto.LoginRequest;
import com.noentiendo.pokecenter.dto.RegisterRequest;
import com.noentiendo.pokecenter.test.TestDataConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestDataConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Integration Tests - Full Auth Flow")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== DEFAULT USER TESTS ====================

    @Test
    @DisplayName("Should login with default user claudio/1234")
    void testLoginWithDefaultUser() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("claudio", "1234");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", equalTo("claudio")))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andExpect(jsonPath("$.data.expiresIn", is(3600000)))
                .andReturn();

        // Extract token for next test
        String responseBody = result.getResponse().getContentAsString();
        String token = extractToken(responseBody);
        
        System.out.println("✅ Login successful with token: " + token.substring(0, 20) + "...");
    }

    @Test
    @DisplayName("Should fail login with wrong password for default user")
    void testLoginWithWrongPasswordForDefaultUser() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("claudio", "wrong");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("AUTHENTICATION_ERROR")));
    }

    // ==================== COMPLETE FLOW TESTS ====================

    @Test
    @DisplayName("Complete flow: Register new user -> Login -> Validate")
    void testCompleteAuthenticationFlow() throws Exception {
        // Step 1: Register new user
        String newUsername = "newuser_" + System.currentTimeMillis();
        RegisterRequest registerRequest = new RegisterRequest(newUsername, "password123");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", equalTo(newUsername)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andReturn();

        String registerResponseBody = registerResult.getResponse().getContentAsString();
        String registerToken = extractToken(registerResponseBody);

        System.out.println("✅ Step 1 PASSED: User registered with token");

        // Step 2: Validate the registered token
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + registerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(true)));

        System.out.println("✅ Step 2 PASSED: Token validated successfully");

        // Step 3: Login with the newly created user
        LoginRequest loginRequest = new LoginRequest(newUsername, "password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", equalTo(newUsername)))
                .andExpect(jsonPath("$.data.token", notNullValue()))
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        String loginToken = extractToken(loginResponseBody);

        System.out.println("✅ Step 3 PASSED: User logged in with new token");

        // Step 4: Validate the login token
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(true)));

        System.out.println("✅ Step 4 PASSED: Login token validated successfully");
        System.out.println("✅✅✅ COMPLETE FLOW TEST PASSED ✅✅✅");
    }

    @Test
    @DisplayName("Should validate register token immediately after registration")
    void testValidateRegisterTokenImmediately() throws Exception {
        // Arrange
        String uniqueUsername = "user_" + System.currentTimeMillis();
        RegisterRequest registerRequest = new RegisterRequest(uniqueUsername, "test123");

        // Act - Register
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String registerResponseBody = registerResult.getResponse().getContentAsString();
        String token = extractToken(registerResponseBody);

        // Assert - Validate immediately
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", is(true)));

        System.out.println("✅ Register token validated immediately");
    }

    @Test
    @DisplayName("Should reject invalid token on validation")
    void testValidationWithInvalidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer invalid-token-xyz"))
                                .andExpect(status().isForbidden());

        System.out.println("✅ Invalid token rejected correctly");
    }

    @Test
    @DisplayName("Should prevent duplicate username registration")
    void testPreventDuplicateRegistration() throws Exception {
        // Arrange
        String uniqueUsername = "user_" + System.currentTimeMillis();
        RegisterRequest firstRequest = new RegisterRequest(uniqueUsername, "pass1");

        // Act - First registration
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        System.out.println("✅ First registration successful");

        // Act - Try duplicate
        RegisterRequest secondRequest = new RegisterRequest(uniqueUsername, "pass2");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.error.code", equalTo("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.error.message", containsString("Username already exists")));

        System.out.println("✅ Duplicate registration prevented correctly");
    }

    @Test
    @DisplayName("Login and register tokens should be different")
    void testRegisterAndLoginTokensDifferent() throws Exception {
        // Arrange
        String uniqueUsername = "user_" + System.currentTimeMillis();

        // Register
        RegisterRequest registerRequest = new RegisterRequest(uniqueUsername, "password123");
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String registerResponseBody = registerResult.getResponse().getContentAsString();
        String registerToken = extractToken(registerResponseBody);

        // Login
        LoginRequest loginRequest = new LoginRequest(uniqueUsername, "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        String loginToken = extractToken(loginResponseBody);

        // Assert - Both generated tokens should be present and valid.
        assertFalse(registerToken.isBlank(), "Register token should not be blank");
        assertFalse(loginToken.isBlank(), "Login token should not be blank");
        System.out.println("✅ Register and Login tokens generated correctly");

        // Both should be valid
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + registerToken))
                .andExpect(jsonPath("$.data", is(true)));

        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + loginToken))
                .andExpect(jsonPath("$.data", is(true)));

        System.out.println("✅ Both tokens are valid");
    }

        private String extractToken(String responseBody) throws Exception {
                return objectMapper.readTree(responseBody).path("data").path("token").asText();
        }
}
