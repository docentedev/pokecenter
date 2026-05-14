package com.noentiendo.pokecenter.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.noentiendo.pokecenter.dto.ApiResponse;
import com.noentiendo.pokecenter.dto.ErrorDetails;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServletWebRequest mockRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        mockRequest = new ServletWebRequest(request);
    }

    // ==================== AUTHENTICATION EXCEPTION TESTS ====================

    @Test
    @DisplayName("Should handle AuthenticationException and return 401")
    void testHandleAuthenticationException() {
        // Arrange
        AuthenticationException ex = new AuthenticationException("Invalid credentials");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleApiException(ex, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertNotNull(response.getBody().error());
        assertEquals("AUTHENTICATION_ERROR", response.getBody().error().code());
        assertEquals("Invalid credentials", response.getBody().error().message());
    }

    // ==================== AUTHORIZATION EXCEPTION TESTS ====================

    @Test
    @DisplayName("Should handle AuthorizationException and return 403")
    void testHandleAuthorizationException() {
        // Arrange
        AuthorizationException ex = new AuthorizationException("Access denied");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleApiException(ex, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("AUTHORIZATION_ERROR", response.getBody().error().code());
    }

    // ==================== VALIDATION EXCEPTION TESTS ====================

    @Test
    @DisplayName("Should handle ValidationException and return 400")
    void testHandleValidationException() {
        // Arrange
        ValidationException ex = new ValidationException("Username cannot be empty");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleApiException(ex, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("VALIDATION_ERROR", response.getBody().error().code());
        assertEquals("Username cannot be empty", response.getBody().error().message());
    }

    // ==================== RESOURCE NOT FOUND EXCEPTION TESTS ====================

    @Test
    @DisplayName("Should handle ResourceNotFoundException and return 404")
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("User not found");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleApiException(ex, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().error().code());
        assertEquals("User not found", response.getBody().error().message());
    }

    // ==================== GENERIC EXCEPTION TESTS ====================

    @Test
    @DisplayName("Should handle generic Exception and return 500")
    void testHandleGenericException() {
        // Arrange
        Exception ex = new Exception("Unexpected error occurred");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleGlobalException(ex, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().error().code());
        assertTrue(response.getBody().error().message().contains("Unexpected error"));
    }

    @Test
    @DisplayName("Should handle NullPointerException and return 500")
    void testHandleNullPointerException() {
        // Arrange
        NullPointerException ex = new NullPointerException("Null value encountered");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleGlobalException(ex, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().error().code());
    }

    @Test
    @DisplayName("Should handle RuntimeException and return 500")
    void testHandleRuntimeException() {
        // Arrange
        RuntimeException ex = new RuntimeException("Runtime error");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleGlobalException(ex, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().success());
    }

    // ==================== RESPONSE STRUCTURE TESTS ====================

    @Test
    @DisplayName("Error response should have correct ApiResponse structure")
    void testErrorResponseStructure() {
        // Arrange
        ValidationException ex = new ValidationException("Test error");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleApiException(ex, mockRequest);

        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertNotNull(response.getBody().error());
        assertNull(response.getBody().data());
        assertEquals("VALIDATION_ERROR", response.getBody().error().code());
        assertEquals("Test error", response.getBody().error().message());
    }

    @Test
    @DisplayName("Error details should contain code and message")
    void testErrorDetailsContent() {
        // Arrange
        AuthenticationException ex = new AuthenticationException("Login failed");

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleApiException(ex, mockRequest);
        ErrorDetails errorDetails = response.getBody().error();

        // Assert
        assertNotNull(errorDetails);
        assertNotNull(errorDetails.code());
        assertNotNull(errorDetails.message());
        assertFalse(errorDetails.code().isBlank());
        assertFalse(errorDetails.message().isBlank());
    }

    // ==================== MULTIPLE EXCEPTION TYPES ====================

    @Test
    @DisplayName("Should handle different exception types correctly")
    void testMultipleExceptionTypes() {
        // Arrange
        ApiException[] exceptions = {
                new AuthenticationException("Auth failed"),
                new AuthorizationException("Auth denied"),
                new ValidationException("Validation error"),
                new ResourceNotFoundException("Not found")
        };

        HttpStatus[] expectedStatuses = {
                HttpStatus.UNAUTHORIZED,
                HttpStatus.FORBIDDEN,
                HttpStatus.BAD_REQUEST,
                HttpStatus.NOT_FOUND
        };

        // Act & Assert
        for (int i = 0; i < exceptions.length; i++) {
            ResponseEntity<ApiResponse<?>> response = 
                exceptionHandler.handleApiException(exceptions[i], mockRequest);
            
            assertEquals(expectedStatuses[i], response.getStatusCode());
            assertFalse(response.getBody().success());
            assertNotNull(response.getBody().error());
        }
    }

    // ==================== EXCEPTION MESSAGE TESTS ====================

    @Test
    @DisplayName("Should preserve original exception message")
    void testPreserveExceptionMessage() {
        // Arrange
        String originalMessage = "Original error message";
        ValidationException ex = new ValidationException(originalMessage);

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleApiException(ex, mockRequest);

        // Assert
        assertEquals(originalMessage, response.getBody().error().message());
    }

    @Test
    @DisplayName("Should handle exception with null message")
    void testHandleExceptionWithNullMessage() {
        // Arrange
        RuntimeException ex = new RuntimeException((String) null);

        // Act
        ResponseEntity<ApiResponse<?>> response = exceptionHandler.handleGlobalException(ex, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().success());
    }
}
