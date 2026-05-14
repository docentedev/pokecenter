package com.noentiendo.pokecenter.filter;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.noentiendo.pokecenter.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtUtil);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    // ==================== VALID TOKEN TESTS ====================

    @Test
    @DisplayName("Should extract and validate valid token from header")
    void testValidTokenInHeader() throws ServletException, IOException {
        // Arrange
        String token = "valid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsername(token)).thenReturn("testuser");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil, times(1)).validateToken(token);
        verify(jwtUtil, times(1)).getUsername(token);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("Should set SecurityContext with valid token")
    void testSecurityContextSetWithValidToken() throws ServletException, IOException {
        // Arrange
        String token = "valid-token";
        String username = "alice";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsername(token)).thenReturn(username);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertNull(SecurityContextHolder.getContext().getAuthentication().getCredentials());
    }

    // ==================== MISSING AUTHORIZATION HEADER TESTS ====================

    @Test
    @DisplayName("Should continue filter chain when Authorization header is missing")
    void testMissingAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        // No Authorization header set

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should continue filter chain when Authorization header is null")
    void testNullAuthorizationHeader() throws ServletException, IOException {
        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Should continue filter chain when Authorization header is empty")
    void testEmptyAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).validateToken(anyString());
    }

    // ==================== INVALID TOKEN TESTS ====================

    @Test
    @DisplayName("Should not set SecurityContext for invalid token")
    void testInvalidToken() throws ServletException, IOException {
        // Arrange
        String token = "invalid-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil, times(1)).validateToken(token);
        verify(jwtUtil, never()).getUsername(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Should continue filter chain even with invalid token")
    void testInvalidTokenContinuesChain() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer invalid-token");
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==================== BEARER PREFIX TESTS ====================

    @Test
    @DisplayName("Should extract token correctly with Bearer prefix")
    void testBearerPrefixExtraction() throws ServletException, IOException {
        // Arrange
        String token = "my-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUsername(token)).thenReturn("user123");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil, times(1)).validateToken(token);
    }

    @Test
    @DisplayName("Should not process token without Bearer prefix")
    void testWithoutBearerPrefix() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "my-jwt-token");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil, never()).validateToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle Bearer prefix with different cases")
    void testBearerPrefixCase() throws ServletException, IOException {
        // Arrange - Bearer prefix should be exact case
        String token = "token123";
        request.addHeader("Authorization", "bearer " + token); // lowercase

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtUtil, never()).validateToken(anyString()); // Should not validate lowercase bearer
    }

    // ==================== EXCEPTION HANDLING TESTS ====================

    @Test
    @DisplayName("Should handle exception during token validation")
    void testExceptionDuringValidation() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer token123");
        when(jwtUtil.validateToken("token123")).thenThrow(new RuntimeException("JWT error"));

        // Act & Assert
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle exception during getUsername")
    void testExceptionDuringGetUsername() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer token123");
        when(jwtUtil.validateToken("token123")).thenReturn(true);
        when(jwtUtil.getUsername("token123")).thenThrow(new RuntimeException("Username error"));

        // Act & Assert
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==================== FILTER CHAIN TESTS ====================

    @Test
    @DisplayName("Should always call filter chain doFilter")
    void testFilterChainAlwaysCalled() throws ServletException, IOException {
        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should call filter chain even when validation fails")
    void testFilterChainCalledOnValidationFailure() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer invalid");
        when(jwtUtil.validateToken("invalid")).thenReturn(false);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
    }

    // ==================== MULTIPLE REQUESTS TESTS ====================

    @Test
    @DisplayName("Should handle multiple requests with different tokens")
    void testMultipleRequestsWithDifferentTokens() throws ServletException, IOException {
        // First request
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token1");
        when(jwtUtil.validateToken("token1")).thenReturn(true);
        when(jwtUtil.getUsername("token1")).thenReturn("user1");

        filter.doFilterInternal(request, response, filterChain);
        assertEquals("user1", SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        // Second request with different token
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token2");
        when(jwtUtil.validateToken("token2")).thenReturn(true);
        when(jwtUtil.getUsername("token2")).thenReturn("user2");

        filter.doFilterInternal(request, response, filterChain);
        assertEquals("user2", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    @DisplayName("Should clear SecurityContext for invalid tokens")
    void testSecurityContextNotSetForInvalidTokens() throws ServletException, IOException {
        // Arrange
        request.addHeader("Authorization", "Bearer invalid");
        when(jwtUtil.validateToken("invalid")).thenReturn(false);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
