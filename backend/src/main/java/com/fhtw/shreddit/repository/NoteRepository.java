package com.fhtw.shreddit.repository;

import com.fhtw.shreddit.model.NoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<NoteEntity, Long> {
    List<NoteEntity> findByDocumentIdOrderByCreatedAtAsc(Long documentId);
}
