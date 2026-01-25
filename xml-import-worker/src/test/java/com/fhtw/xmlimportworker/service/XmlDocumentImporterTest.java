package com.fhtw.xmlimportworker.service;

import com.fhtw.xmlimportworker.model.DocumentEntity;
import com.fhtw.xmlimportworker.model.ImportedDocument;
import com.fhtw.xmlimportworker.repo.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XmlDocumentImporterTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private XmlDocumentImporter importer;

    @Test
    void importsDocumentAndSetsSummaryStatusAndTags() {
        ImportedDocument input = new ImportedDocument(
                "Budget Report",
                "budget-2026.pdf",
                "content",
                "summary",
                "alice",
                LocalDate.of(2026, 1, 19),
                List.of(" finance ", "2026")
        );

        when(documentRepository.save(any(DocumentEntity.class)))
                .thenAnswer(invocation -> {
                    DocumentEntity entity = invocation.getArgument(0);
                    entity.setId(1L);
                    return entity;
                });

        DocumentEntity saved = importer.importDocument(input);

        ArgumentCaptor<DocumentEntity> captor = ArgumentCaptor.forClass(DocumentEntity.class);
        verify(documentRepository, times(1)).save(captor.capture());

        DocumentEntity entity = captor.getValue();
        assertEquals("Budget Report", entity.getTitle());
        assertEquals("budget-2026.pdf", entity.getFilename());
        assertEquals("content", entity.getContent());
        assertEquals("summary", entity.getSummary());
        assertEquals("alice", entity.getUsername());
        assertEquals(LocalDate.of(2026, 1, 19), entity.getDocumentDate());
        assertEquals("finance,2026", entity.getTags());
        assertEquals("OK", entity.getSummaryStatus());
        assertNotNull(entity.getCreatedAt());

        assertEquals(1L, saved.getId());
    }

    @Test
    void marksImportedWhenSummaryMissingAndSkipsEmptyTags() {
        ImportedDocument input = new ImportedDocument(
                "Notes",
                "notes.txt",
                null,
                null,
                "xml-import",
                null,
                List.of(" ", "")
        );

        when(documentRepository.save(any(DocumentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentEntity saved = importer.importDocument(input);

        assertEquals("IMPORTED", saved.getSummaryStatus());
        assertNull(saved.getTags());
    }
}
