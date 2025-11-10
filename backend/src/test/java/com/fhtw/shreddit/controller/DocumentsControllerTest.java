package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentsControllerTest {

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private DocumentsController documentsController;

    private DocumentDto testDocument1;
    private DocumentDto testDocument2;

    @BeforeEach
    void setUp() {
        // Create test documents
        testDocument1 = new DocumentDto(1L, "Test Document 1", "Content 1", LocalDateTime.now(), "testuser");
        testDocument2 = new DocumentDto(2L, "Test Document 2", "Content 2", LocalDateTime.now(), "testuser");
    }

    @Test
    void getDocumentsReturnsAllDocuments() {
        // Arrange
        List<DocumentDto> documents = Arrays.asList(testDocument1, testDocument2);
        when(documentService.getAll()).thenReturn(documents);

        // Act
        ResponseEntity<List<DocumentDto>> response = documentsController.getDocuments();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(testDocument1.getId(), response.getBody().get(0).getId());
        assertEquals(testDocument2.getId(), response.getBody().get(1).getId());
        verify(documentService, times(1)).getAll();
    }

    @Test
    void getDocumentReturnsDocumentWhenFound() {
        // Arrange
        when(documentService.getById(1L)).thenReturn(Optional.of(testDocument1));

        // Act
        ResponseEntity<DocumentDto> response = documentsController.getDocument(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testDocument1.getId(), response.getBody().getId());
        assertEquals(testDocument1.getTitle(), response.getBody().getTitle());
        verify(documentService, times(1)).getById(1L);
    }

    @Test
    void getDocumentReturnsNotFoundWhenDocumentDoesNotExist() {
        // Arrange
        when(documentService.getById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<DocumentDto> response = documentsController.getDocument(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(documentService, times(1)).getById(999L);
    }

    @Test
    void createDocumentReturnsCreatedWithDocumentInfo() {
        // Arrange
        DocumentDto newDocument = new DocumentDto(null, "New Document", "New Content", null, "testuser");
        DocumentDto createdDocument = new DocumentDto(3L, "New Document", "New Content", LocalDateTime.now(), "testuser");

        when(documentService.create(newDocument)).thenReturn(createdDocument);

        // Act
        ResponseEntity<DocumentDto> response = documentsController.createDocument(newDocument);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdDocument.getId(), response.getBody().getId());
        assertEquals(createdDocument.getTitle(), response.getBody().getTitle());
        verify(documentService, times(1)).create(newDocument);
    }

    @Test
    void deleteDocumentReturnsNoContent() {
        // Arrange
        doNothing().when(documentService).delete(1L);

        // Act
        ResponseEntity<Void> response = documentsController.deleteDocument(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(documentService, times(1)).delete(1L);
    }
}
