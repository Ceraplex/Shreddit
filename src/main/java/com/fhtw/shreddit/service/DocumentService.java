package com.fhtw.shreddit.service;

import com.fhtw.shreddit.model.Document;
import com.fhtw.shreddit.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public List<Document> getAll() {
        return repository.findAll();
    }

    public Document create(Document doc) {
        return repository.save(doc);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
