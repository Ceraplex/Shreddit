package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.CommentDto;
import com.fhtw.shreddit.exception.DocumentException;
import com.fhtw.shreddit.model.CommentEntity;
import com.fhtw.shreddit.model.DocumentEntity;
import com.fhtw.shreddit.repository.CommentRepository;
import com.fhtw.shreddit.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private CommentService commentService;

    private DocumentEntity document;
    private CommentEntity comment;

    @BeforeEach
    void setUp() {
        document = new DocumentEntity();
        document.setId(1L);
        document.setTitle("Test Document");

        comment = new CommentEntity();
        comment.setId(1L);
        comment.setDocumentId(1L);
        comment.setText("Test comment");
        comment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void listComments_ShouldReturnComments() {
        // Given
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(commentRepository.findByDocumentIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(comment));

        // When
        List<CommentDto> comments = commentService.listComments(1L);

        // Then
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getId()).isEqualTo(1L);
        assertThat(comments.get(0).getText()).isEqualTo("Test comment");
        verify(commentRepository).findByDocumentIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void listComments_ShouldThrowExceptionWhenDocumentNotFound() {
        // Given
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(DocumentException.class, () -> commentService.listComments(999L));
        verify(commentRepository, never()).findByDocumentIdOrderByCreatedAtDesc(any());
    }

    @Test
    void addComment_ShouldCreateComment() {
        // Given
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(comment);

        // When
        CommentDto result = commentService.addComment(1L, "Test comment");

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Test comment");
        verify(commentRepository).save(any(CommentEntity.class));
    }

    @Test
    void addComment_ShouldThrowExceptionWhenTextIsBlank() {
        // When/Then
        assertThrows(DocumentException.class, () -> commentService.addComment(1L, ""));
        assertThrows(DocumentException.class, () -> commentService.addComment(1L, null));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_ShouldThrowExceptionWhenDocumentNotFound() {
        // Given
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(DocumentException.class, () -> commentService.addComment(999L, "Test comment"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteComment_ShouldDeleteComment() {
        // Given
        when(commentRepository.existsById(1L)).thenReturn(true);
        doNothing().when(commentRepository).deleteById(1L);

        // When
        commentService.deleteComment(1L);

        // Then
        verify(commentRepository).deleteById(1L);
    }

    @Test
    void deleteComment_ShouldThrowExceptionWhenCommentNotFound() {
        // Given
        when(commentRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThrows(DocumentException.class, () -> commentService.deleteComment(999L));
        verify(commentRepository, never()).deleteById(any());
    }
}