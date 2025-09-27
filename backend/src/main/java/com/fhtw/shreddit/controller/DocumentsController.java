package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.model.Document;
import com.fhtw.shreddit.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DocumentsController {

    private final DocumentService documentService;

    public DocumentsController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/documents")
    public ResponseEntity<List<Document>> getDocuments() {
        return ResponseEntity.ok(documentService.getAll());
    }

    @PostMapping(value = "/documents", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Document> createDocument(@RequestBody Document document) {
        Document created = documentService.create(document);
        return ResponseEntity.status(201).body(created);
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
