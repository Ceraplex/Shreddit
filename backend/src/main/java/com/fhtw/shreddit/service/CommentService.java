package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.CommentDto;
import com.fhtw.shreddit.exception.DocumentException;
import com.fhtw.shreddit.model.CommentEntity;
import com.fhtw.shreddit.model.DocumentEntity;
import com.fhtw.shreddit.repository.CommentRepository;
import com.fhtw.shreddit.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing comments on documents.
 */
@Service
public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final DocumentRepository documentRepository;

    public CommentService(CommentRepository commentRepository, DocumentRepository documentRepository) {
        this.commentRepository = commentRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * List all comments for a document, ordered by creation date (newest first).
     *
     * @param documentId the ID of the document
     * @return list of comments for the document
     * @throws DocumentException if the document doesn't exist
     */
    public List<CommentDto> listComments(Long documentId) {
        // Verify document exists
        ensureDocumentExists(documentId);
        
        return commentRepository.findByDocumentIdOrderByCreatedAtDesc(documentId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Add a comment to a document.
     *
     * @param documentId the ID of the document
     * @param text the text of the comment
     * @return the created comment
     * @throws DocumentException if the document doesn't exist or the text is blank
     */
    public CommentDto addComment(Long documentId, String text) {
        if (text == null || text.isBlank()) {
            throw new DocumentException("Comment text must not be blank");
        }
        
        // Verify document exists
        ensureDocumentExists(documentId);

        CommentEntity entity = new CommentEntity();
        entity.setDocumentId(documentId);
        entity.setText(text.trim());

        CommentEntity saved = commentRepository.save(entity);
        log.info("Created comment {} for document {}", saved.getId(), documentId);
        return toDto(saved);
    }

    /**
     * Delete a comment by ID.
     *
     * @param commentId the ID of the comment to delete
     * @throws DocumentException if the comment doesn't exist
     */
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new DocumentException("Comment not found");
        }
        commentRepository.deleteById(commentId);
        log.info("Deleted comment {}", commentId);
    }

    /**
     * Ensure a document exists.
     *
     * @param documentId the ID of the document
     * @return the document entity
     * @throws DocumentException if the document doesn't exist
     */
    private DocumentEntity ensureDocumentExists(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException("Document not found"));
    }

    /**
     * Convert a comment entity to a DTO.
     *
     * @param entity the comment entity
     * @return the comment DTO
     */
    private CommentDto toDto(CommentEntity entity) {
        CommentDto dto = new CommentDto();
        dto.setId(entity.getId());
        dto.setDocumentId(entity.getDocumentId());
        dto.setText(entity.getText());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}