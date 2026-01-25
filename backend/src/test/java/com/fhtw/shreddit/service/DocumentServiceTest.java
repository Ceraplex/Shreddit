package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.api.dto.OcrRequestDto;
import com.fhtw.shreddit.exception.DocumentCreationException;
import com.fhtw.shreddit.model.DocumentEntity;
import com.fhtw.shreddit.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private RabbitMQService rabbitMQService;

    @InjectMocks
    private DocumentService documentService;

    private DocumentEntity testEntity1;
    private DocumentEntity testEntity2;
    private DocumentDto testDto1;
    private DocumentDto testDto2;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("testuser", "pw"));
        now = LocalDateTime.now();

        // Create test entities
        testEntity1 = new DocumentEntity();
        testEntity1.setId(1L);
        testEntity1.setTitle("Test Document 1");
        testEntity1.setContent("Content 1");
        testEntity1.setCreatedAt(now);
        testEntity1.setUsername("testuser");

        testEntity2 = new DocumentEntity();
        testEntity2.setId(2L);
        testEntity2.setTitle("Test Document 2");
        testEntity2.setContent("Content 2");
        testEntity2.setCreatedAt(now);
        testEntity2.setUsername("testuser");

        // Create test DTOs
        testDto1 = new DocumentDto(1L, "Test Document 1", "Content 1", now, "testuser");
        testDto2 = new DocumentDto(2L, "Test Document 2", "Content 2", now, "testuser");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllReturnsAllDocuments() {
        // Arrange
        when(documentRepository.findAll()).thenReturn(Arrays.asList(testEntity1, testEntity2));

        // Act
        List<DocumentDto> result = documentService.getAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(testDto1.getId(), result.get(0).getId());
        assertEquals(testDto1.getTitle(), result.get(0).getTitle());
        assertEquals(testDto2.getId(), result.get(1).getId());
        assertEquals(testDto2.getTitle(), result.get(1).getTitle());
        verify(documentRepository, times(1)).findAll();
    }

    @Test
    void getByIdReturnsDocumentWhenFound() {
        // Arrange
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testEntity1));

        // Act
        Optional<DocumentDto> result = documentService.getById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testDto1.getId(), result.get().getId());
        assertEquals(testDto1.getTitle(), result.get().getTitle());
        verify(documentRepository, times(1)).findById(1L);
    }

    @Test
    void getByIdReturnsEmptyWhenNotFound() {
        // Arrange
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<DocumentDto> result = documentService.getById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(documentRepository, times(1)).findById(999L);
    }

    @Test
    void createSavesDocumentAndSendsOcrRequest() {
        // Arrange
        DocumentDto newDto = new DocumentDto(null, "New Document", "New Content", null, "testuser");
        DocumentEntity savedEntity = new DocumentEntity();
        savedEntity.setId(3L);
        savedEntity.setTitle("New Document");
        savedEntity.setContent("New Content");
        savedEntity.setCreatedAt(now);

        when(documentRepository.save(any(DocumentEntity.class))).thenReturn(savedEntity);
        doNothing().when(rabbitMQService).sendOcrRequest(any(OcrRequestDto.class));

        // Act
        DocumentDto result = documentService.create(newDto);

        // Assert
        assertEquals(3L, result.getId());
        assertEquals("New Document", result.getTitle());
        assertEquals("New Content", result.getContent());
        assertNotNull(result.getCreatedAt());

        verify(documentRepository, times(1)).save(any(DocumentEntity.class));
        verify(rabbitMQService, times(1)).sendOcrRequest(any(OcrRequestDto.class));
    }

    @Test
    void createThrowsExceptionWhenSaveFails() {
        // Arrange
        DocumentDto newDto = new DocumentDto(null, "New Document", "New Content", null, "testuser");
        when(documentRepository.save(any(DocumentEntity.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(DocumentCreationException.class, () -> documentService.create(newDto));
        verify(documentRepository, times(1)).save(any(DocumentEntity.class));
        verify(rabbitMQService, never()).sendOcrRequest(any(OcrRequestDto.class));
    }

    @Test
    void deleteCallsRepositoryDeleteById() {
        // Arrange
        doNothing().when(documentRepository).deleteById(1L);

        // Act
        documentService.delete(1L);

        // Assert
        verify(documentRepository, times(1)).deleteById(1L);
    }
}
