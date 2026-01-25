package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.CommentDto;
import com.fhtw.shreddit.api.dto.CreateCommentRequest;
import com.fhtw.shreddit.service.CommentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing comments on documents.
 */
@RestController
public class DocumentCommentsController {
    private static final Logger log = LoggerFactory.getLogger(DocumentCommentsController.class);

    private final CommentService commentService;

    public DocumentCommentsController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Get all comments for a document.
     *
     * @param documentId the ID of the document
     * @return list of comments for the document
     */
    @GetMapping("/documents/{documentId}/comments")
    public ResponseEntity<List<CommentDto>> listComments(@PathVariable("documentId") Long documentId) {
        return ResponseEntity.ok(commentService.listComments(documentId));
    }

    /**
     * Add a comment to a document.
     *
     * @param documentId the ID of the document
     * @param request the comment creation request
     * @return the created comment
     */
    @PostMapping("/documents/{documentId}/comments")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable("documentId") Long documentId,
            @Valid @RequestBody CreateCommentRequest request) {
        log.debug("Adding comment for document {} with text: {}", documentId, request.getText());
        CommentDto created = commentService.addComment(documentId, request.getText());
        return ResponseEntity.status(201).body(created);
    }

    /**
     * Delete a comment.
     *
     * @param commentId the ID of the comment to delete
     * @return no content response
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}