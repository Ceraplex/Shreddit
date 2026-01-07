package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.NoteDto;
import com.fhtw.shreddit.exception.DocumentException;
import com.fhtw.shreddit.model.DocumentEntity;
import com.fhtw.shreddit.model.NoteEntity;
import com.fhtw.shreddit.repository.DocumentRepository;
import com.fhtw.shreddit.repository.NoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {
    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    private final NoteRepository noteRepository;
    private final DocumentRepository documentRepository;

    public NoteService(NoteRepository noteRepository, DocumentRepository documentRepository) {
        this.noteRepository = noteRepository;
        this.documentRepository = documentRepository;
    }

    public List<NoteDto> listNotes(Long documentId) {
        String currentUser = currentUser();
        ensureDocumentAccessible(documentId, currentUser);
        return noteRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public NoteDto addNote(Long documentId, String content) {
        if (content == null || content.isBlank()) {
            throw new DocumentException("Note content must not be empty");
        }
        String currentUser = currentUser();
        DocumentEntity doc = ensureDocumentAccessible(documentId, currentUser);

        NoteEntity entity = new NoteEntity();
        entity.setDocumentId(doc.getId());
        entity.setContent(content.trim());
        entity.setAuthor(currentUser);

        NoteEntity saved = noteRepository.save(entity);
        log.info("Created note {} for doc {} by {}", saved.getId(), documentId, currentUser);
        return toDto(saved);
    }

    private DocumentEntity ensureDocumentAccessible(Long documentId, String currentUser) {
        DocumentEntity doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException("Document not found"));
        if (doc.getUsername() != null && !doc.getUsername().equals(currentUser)) {
            throw new DocumentException("Not allowed to access this document");
        }
        return doc;
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "anonymous";
    }

    private NoteDto toDto(NoteEntity entity) {
        NoteDto dto = new NoteDto();
        dto.setId(entity.getId());
        dto.setDocumentId(entity.getDocumentId());
        dto.setAuthor(entity.getAuthor());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
