package com.fhtw.shreddit.repository;

import com.fhtw.shreddit.model.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for managing Comment entities.
 */
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    /**
     * Find all comments for a document, ordered by creation date in descending order (newest first).
     *
     * @param documentId the ID of the document
     * @return list of comments for the document
     */
    List<CommentEntity> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
}