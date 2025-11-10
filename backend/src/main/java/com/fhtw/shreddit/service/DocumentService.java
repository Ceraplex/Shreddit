package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.api.dto.OcrRequestDto;
import com.fhtw.shreddit.exception.DocumentCreationException;
import com.fhtw.shreddit.model.DocumentEntity;
import com.fhtw.shreddit.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository repository;
    private final RabbitMQService rabbitMQService;

    public DocumentService(DocumentRepository repository, RabbitMQService rabbitMQService) {
        this.repository = repository;
        this.rabbitMQService = rabbitMQService;
    }

    public List<DocumentDto> getAll() {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        return repository.findAll().stream()
                .filter(doc -> doc.getUsername() == null || doc.getUsername().equals(username))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<DocumentDto> getById(Long id) {
        return repository.findById(id).map(this::toDto);
    }

    public DocumentDto create(DocumentDto doc) {
        try {
            log.info("Creating document: {}", doc.getTitle());
            DocumentEntity entity = toEntity(doc);
            DocumentEntity saved = repository.save(entity);
            DocumentDto createdDoc = toDto(saved);

            // Send OCR request
            OcrRequestDto ocrRequest = new OcrRequestDto(saved.getId(), saved.getTitle());
            rabbitMQService.sendOcrRequest(ocrRequest);

            return createdDoc;
        } catch (Exception e) {
            log.error("Error creating document: {}", e.getMessage(), e);
            throw new DocumentCreationException("Failed to create document", e);
        }
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private DocumentEntity toEntity(DocumentDto doc) {
        DocumentEntity entity = new DocumentEntity();
        entity.setId(doc.getId());
        entity.setTitle(doc.getTitle());
        entity.setContent(doc.getContent());
        entity.setCreatedAt(doc.getCreatedAt());
        entity.setUsername(doc.getUsername());
        entity.setFilename(doc.getFilename());
        return entity;
    }

    private DocumentDto toDto(DocumentEntity entity) {
        DocumentDto doc = new DocumentDto();
        doc.setId(entity.getId());
        doc.setTitle(entity.getTitle());
        doc.setContent(entity.getContent());
        doc.setCreatedAt(entity.getCreatedAt());
        doc.setUsername(entity.getUsername());
        doc.setFilename(entity.getFilename());
        return doc;
    }
}
