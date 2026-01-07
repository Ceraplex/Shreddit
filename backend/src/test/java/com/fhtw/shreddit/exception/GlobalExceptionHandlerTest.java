package com.fhtw.shreddit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDocumentCreationException() {
        DocumentCreationException ex = new DocumentCreationException("test document creation error");
        ResponseEntity<ErrorResponse> response = handler.handleDocumentCreationException(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("Failed to create document", errorResponse.getMessage());
        assertEquals("test document creation error", errorResponse.getDetails());
    }

    @Test
    void handleMessagePublishException() {
        MessagePublishException ex = new MessagePublishException("test message publish error");
        ResponseEntity<ErrorResponse> response = handler.handleMessagePublishException(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("Failed to process document for OCR", errorResponse.getMessage());
        assertEquals("test message publish error", errorResponse.getDetails());
    }

    @Test
    void handleDocumentException() {
        DocumentException ex = new DocumentException("doc error");
        ResponseEntity<ErrorResponse> response = handler.handleDocumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Document error", errorResponse.getMessage());
        assertEquals("doc error", errorResponse.getDetails());
    }

    @Test
    void handleGenericException() {
        Exception ex = new RuntimeException("test generic error");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("An unexpected error occurred", errorResponse.getMessage());
        assertEquals("test generic error", errorResponse.getDetails());
    }
}
