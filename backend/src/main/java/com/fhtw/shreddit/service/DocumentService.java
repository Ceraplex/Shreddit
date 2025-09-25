package com.fhtw.shreddit.service;

import com.fhtw.shreddit.model.Document;
import com.fhtw.shreddit.model.DocumentEntity;
import com.fhtw.shreddit.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public List<Document> getAll() {
        return repository.findAll().stream()
                .map(this::toApiModel)
                .collect(Collectors.toList());
    }

    public Document create(Document doc) {
        DocumentEntity entity = toEntity(doc);
        DocumentEntity saved = repository.save(entity);
        return toApiModel(saved);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private DocumentEntity toEntity(Document doc) {
        DocumentEntity entity = new DocumentEntity();
        entity.setId(doc.getId());
        entity.setTitle(doc.getTitle());
        entity.setContent(doc.getContent());
        return entity;
    }

    private Document toApiModel(DocumentEntity entity) {
        Document doc = new Document();
        doc.setId(entity.getId());
        doc.setTitle(entity.getTitle());
        doc.setContent(entity.getContent());
        return doc;
    }
}
