package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.CommentDto;
import com.fhtw.shreddit.api.dto.CreateCommentRequest;
import com.fhtw.shreddit.exception.DocumentException;
import com.fhtw.shreddit.service.CommentService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentCommentsControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private DocumentCommentsController controller;

    private CommentDto comment;
    private CreateCommentRequest validRequest;

    @BeforeEach
    void setUp() {
        comment = new CommentDto();
        comment.setId(1L);
        comment.setDocumentId(5L);
        comment.setText("Test comment");
        comment.setCreatedAt(LocalDateTime.now());

        validRequest = new CreateCommentRequest();
        validRequest.setText("Test comment");
    }

    @Test
    void listComments_ShouldReturnComments() {
        // Given
        when(commentService.listComments(5L)).thenReturn(Arrays.asList(comment));

        // When
        ResponseEntity<List<CommentDto>> response = controller.listComments(5L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getId()).isEqualTo(1L);
        verify(commentService).listComments(5L);
    }

    @Test
    void addComment_ShouldCreateComment() {
        // Given
        when(commentService.addComment(eq(5L), any(String.class))).thenReturn(comment);

        // When
        ResponseEntity<CommentDto> response = controller.addComment(5L, validRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isEqualTo(1L);
        verify(commentService).addComment(eq(5L), eq("Test comment"));
    }

    @Test
    void deleteComment_ShouldReturnNoContent() {
        // Given
        doNothing().when(commentService).deleteComment(1L);

        // When
        ResponseEntity<Void> response = controller.deleteComment(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(commentService).deleteComment(1L);
    }
}