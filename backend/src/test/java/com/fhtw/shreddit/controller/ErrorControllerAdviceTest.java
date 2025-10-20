package com.fhtw.shreddit.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErrorControllerAdviceTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ErrorControllerAdvice errorController;

    @BeforeEach
    void setUp() {
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void handleErrorWithStatusCodeReturnsCorrectResponse() {
        // Arrange
        when(request.getAttribute("jakarta.servlet.error.status_code")).thenReturn(404);
        when(request.getAttribute("jakarta.servlet.error.request_uri")).thenReturn("/api/nonexistent");

        // Act
        ResponseEntity<Map<String, Object>> response = errorController.handleError(request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("/api/nonexistent", body.get("path"));
        assertEquals("GET", body.get("method"));
        assertEquals("Not found. Use /api/... from the UI (POST for login/register).", body.get("message"));
    }

    @Test
    void handleErrorWithNullStatusCodeDefaultsTo500() {
        // Arrange
        when(request.getAttribute("jakarta.servlet.error.status_code")).thenReturn(null);
        when(request.getAttribute("jakarta.servlet.error.request_uri")).thenReturn("/api/error");

        // Act
        ResponseEntity<Map<String, Object>> response = errorController.handleError(request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
    }

    @Test
    void handleErrorWithNullRequestUriUsesRequestURI() {
        // Arrange
        when(request.getAttribute("jakarta.servlet.error.status_code")).thenReturn(400);
        when(request.getAttribute("jakarta.servlet.error.request_uri")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/error");

        // Act
        ResponseEntity<Map<String, Object>> response = errorController.handleError(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("/error", body.get("path"));
    }

    @Test
    void handleErrorWithCustomStatusCodeReturnsCorrectResponse() {
        // Arrange
        when(request.getAttribute("jakarta.servlet.error.status_code")).thenReturn(418); // I'm a teapot
        when(request.getAttribute("jakarta.servlet.error.request_uri")).thenReturn("/api/coffee");
        when(request.getMethod()).thenReturn("POST");

        // Act
        ResponseEntity<Map<String, Object>> response = errorController.handleError(request);

        // Assert
        assertEquals(HttpStatus.I_AM_A_TEAPOT, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(418, body.get("status"));
        assertEquals("I'm a teapot", body.get("error"));
        assertEquals("/api/coffee", body.get("path"));
        assertEquals("POST", body.get("method"));
    }
}