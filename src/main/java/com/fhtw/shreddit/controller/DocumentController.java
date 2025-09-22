package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.DocumentsApi;
import com.fhtw.shreddit.model.Document;
import com.fhtw.shreddit.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DocumentController implements DocumentsApi {
    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<Document>> getDocuments() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<Document> createDocument(Document document) {
        return ResponseEntity.status(201).body(service.create(document));
    }

    @Override
    public ResponseEntity<Void> deleteDocument(Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
