package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.service.DocumentService;
import com.fhtw.shreddit.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class DocumentsController {

    private static final Logger log = LoggerFactory.getLogger(DocumentsController.class);

    private final DocumentService documentService;
    private final StorageService storageService;

    public DocumentsController(DocumentService documentService, StorageService storageService) {
        this.documentService = documentService;
        this.storageService = storageService;
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentDto>> getDocuments() {
        return ResponseEntity.ok(documentService.getAll());
    }

    // Alias with /api prefix for frontend compatibility
    @GetMapping("/api/documents")
    public ResponseEntity<List<DocumentDto>> getDocumentsApi() {
        return ResponseEntity.ok(documentService.getAll());
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable Long id) {
        return documentService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Alias with /api prefix for frontend compatibility
    @GetMapping("/api/documents/{id}")
    public ResponseEntity<DocumentDto> getDocumentApi(@PathVariable Long id) {
        return documentService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/documents")
    public ResponseEntity<DocumentDto> createDocument(@RequestBody DocumentDto document) {
        log.debug("Creating document: title='{}'", document.getTitle());
        DocumentDto created = documentService.create(document);
        log.debug("Created document with id={}", created.getId());
        return ResponseEntity.status(201).body(created);
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        // If document does not exist, return 204 to keep API idempotent
        Optional<DocumentDto> docOpt = documentService.getById(id);
        if (docOpt.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        DocumentDto doc = docOpt.get();

        // Authorization: only owner (username match) can delete; if username is null, allow for backward compatibility
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = (auth != null ? auth.getName() : null);
        boolean isOwner = (doc.getUsername() == null) || (currentUser != null && doc.getUsername().equals(currentUser));
        if (!isOwner) {
            return ResponseEntity.status(403).build();
        }

        // Best-effort delete in storage: prefer exact stored filename; fallback to title for legacy records
        try {
            String objectName = (doc.getFilename() != null && !doc.getFilename().isBlank())
                    ? doc.getFilename()
                    : doc.getTitle();
            if (objectName != null && !objectName.isBlank()) {
                storageService.deleteObject(objectName);
            }
        } catch (Exception e) {
            log.warn("Failed to delete MinIO object for document id={}: {}", id, e.getMessage());
        }

        // Delete DB record
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
