package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.OcrRequestDto;
import com.fhtw.shreddit.service.RabbitMQService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RootControllerTest {

    @Mock
    private RabbitMQService rabbitMQService;

    @InjectMocks
    private RootController rootController;

    @Test
    void rootEndpointReturnsOkWithMessage() {
        ResponseEntity<String> response = rootController.root();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Shreddit backend is up", response.getBody());
    }

    @Test
    void faviconEndpointReturnsNoContent() {
        ResponseEntity<byte[]> response = rootController.favicon();
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNotNull(response.getHeaders().getCacheControl());
        assertTrue(response.getHeaders().getCacheControl().contains("public, max-age=86400"));
    }

    @Test
    void uploadPdfWithEmptyFileReturnsBadRequest() {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", new byte[0]
        );
        
        ResponseEntity<?> response = rootController.uploadPdf(emptyFile, "Test Title");
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Please upload a file", response.getBody());
    }

    @Test
    void uploadPdfWithValidFileReturnsOkWithDocumentInfo() throws Exception {
        // Arrange
        MockMultipartFile validFile = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", "PDF content".getBytes()
        );
        doNothing().when(rabbitMQService).sendOcrRequest(any(OcrRequestDto.class));
        
        // Act
        ResponseEntity<?> response = rootController.uploadPdf(validFile, "Test Title");
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertNotNull(responseBody.get("id"));
        assertEquals("Test Title", responseBody.get("title"));
        assertNotNull(responseBody.get("createdAt"));
        assertEquals("test.pdf", responseBody.get("filename"));
        
        // Verify that sendOcrRequest was called once
        verify(rabbitMQService, times(1)).sendOcrRequest(any(OcrRequestDto.class));
    }

    @Test
    void uploadPdfWithValidFileButNoTitleUsesFilenameAsTitle() throws Exception {
        // Arrange
        MockMultipartFile validFile = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", "PDF content".getBytes()
        );
        doNothing().when(rabbitMQService).sendOcrRequest(any(OcrRequestDto.class));
        
        // Act
        ResponseEntity<?> response = rootController.uploadPdf(validFile, null);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertEquals("test.pdf", responseBody.get("title"));
    }

    @Test
    void uploadPdfWithExceptionReturnsInternalServerError() throws Exception {
        // Arrange
        MockMultipartFile validFile = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", "PDF content".getBytes()
        );
        doThrow(new RuntimeException("Test exception")).when(rabbitMQService).sendOcrRequest(any(OcrRequestDto.class));
        
        // Act
        ResponseEntity<?> response = rootController.uploadPdf(validFile, "Test Title");
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertTrue(((String) responseBody.get("error")).contains("Error processing PDF document"));
    }
}