package com.fhtw.xmlimportworker.service;

import com.fhtw.xmlimportworker.model.DocumentEntity;
import com.fhtw.xmlimportworker.model.ImportedDocument;
import com.fhtw.xmlimportworker.repo.DocumentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@Service
public class XmlDocumentImporter {
    private final DocumentRepository documentRepository;

    public XmlDocumentImporter(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public DocumentEntity importDocument(ImportedDocument importedDocument) {
        DocumentEntity entity = new DocumentEntity();
        entity.setTitle(importedDocument.title());
        entity.setFilename(importedDocument.filename());
        entity.setContent(importedDocument.content());
        entity.setSummary(importedDocument.summary());
        entity.setUsername(importedDocument.username());
        entity.setDocumentDate(importedDocument.documentDate());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setSummaryStatus(importedDocument.summary() != null && !importedDocument.summary().isBlank()
                ? "OK"
                : "IMPORTED");
        entity.setOcrText(null);

        if (importedDocument.tags() != null && !importedDocument.tags().isEmpty()) {
            StringJoiner joiner = new StringJoiner(",");
            for (String tag : importedDocument.tags()) {
                if (tag != null && !tag.isBlank()) {
                    joiner.add(tag.trim());
                }
            }
            String tagValue = joiner.toString();
            entity.setTags(tagValue.isBlank() ? null : tagValue);
        }

        return documentRepository.save(entity);
    }
}
