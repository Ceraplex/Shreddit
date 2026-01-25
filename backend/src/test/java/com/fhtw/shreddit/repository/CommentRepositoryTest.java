package com.fhtw.shreddit.repository;

import com.fhtw.shreddit.model.CommentEntity;
import com.fhtw.shreddit.model.DocumentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private DocumentEntity document;
    private CommentEntity comment1;
    private CommentEntity comment2;

    @BeforeEach
    void setUp() {
        // Create a test document
        document = new DocumentEntity();
        document.setTitle("Test Document");
        document.setContent("Test Content");
        entityManager.persist(document);
        entityManager.flush();

        // Create test comments with different timestamps
        comment1 = new CommentEntity();
        comment1.setDocumentId(document.getId());
        comment1.setText("First comment");
        comment1.setCreatedAt(LocalDateTime.now().minusHours(1)); // Older comment
        entityManager.persist(comment1);

        comment2 = new CommentEntity();
        comment2.setDocumentId(document.getId());
        comment2.setText("Second comment");
        comment2.setCreatedAt(LocalDateTime.now()); // Newer comment
        entityManager.persist(comment2);

        entityManager.flush();
    }

    @Test
    void findByDocumentIdOrderByCreatedAtDesc_ShouldReturnCommentsInDescendingOrder() {
        // When
        List<CommentEntity> comments = commentRepository.findByDocumentIdOrderByCreatedAtDesc(document.getId());

        // Then
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getId()).isEqualTo(comment2.getId()); // Newer comment should be first
        assertThat(comments.get(1).getId()).isEqualTo(comment1.getId()); // Older comment should be second
    }

    @Test
    void findByDocumentIdOrderByCreatedAtDesc_ShouldReturnEmptyListForNonExistentDocument() {
        // When
        List<CommentEntity> comments = commentRepository.findByDocumentIdOrderByCreatedAtDesc(999L);

        // Then
        assertThat(comments).isEmpty();
    }
}