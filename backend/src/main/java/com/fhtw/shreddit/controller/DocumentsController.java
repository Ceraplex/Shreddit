package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DocumentsController {

    private static final Logger log = LoggerFactory.getLogger(DocumentsController.class);

    private final DocumentService documentService;

    public DocumentsController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentDto>> getDocuments() {
        return ResponseEntity.ok(documentService.getAll());
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
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
