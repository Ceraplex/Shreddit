package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.DocumentDto;
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

    public List<DocumentDto> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DocumentDto create(DocumentDto doc) {
        DocumentEntity entity = toEntity(doc);
        DocumentEntity saved = repository.save(entity);
        return toDto(saved);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private DocumentEntity toEntity(DocumentDto doc) {
        DocumentEntity entity = new DocumentEntity();
        entity.setId(doc.getId());
        entity.setTitle(doc.getTitle());
        entity.setContent(doc.getContent());
        return entity;
    }

    private DocumentDto toDto(DocumentEntity entity) {
        DocumentDto doc = new DocumentDto();
        doc.setId(entity.getId());
        doc.setTitle(entity.getTitle());
        doc.setContent(entity.getContent());
        return doc;
    }
}
