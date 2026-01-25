package com.fhtw.xmlimportworker.integration;

import com.fhtw.xmlimportworker.model.DocumentEntity;
import com.fhtw.xmlimportworker.repo.DocumentRepository;
import com.fhtw.xmlimportworker.service.XmlImportScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class XmlImportEndToEndTest {

    private static final Path BASE_DIR = createTempDir();
    private static final Path INBOX = BASE_DIR.resolve("inbox");
    private static final Path ARCHIVE = BASE_DIR.resolve("archive");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("xmlimport.input-dir", () -> INBOX.toString());
        registry.add("xmlimport.archive-dir", () -> ARCHIVE.toString());
        registry.add("xmlimport.file-pattern", () -> "*.xml");
    }

    @Autowired
    private XmlImportScheduler scheduler;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void importsXmlIntoDatabaseAndArchivesFile() throws Exception {
        Files.createDirectories(INBOX);
        Files.createDirectories(ARCHIVE);

        String xml = """
                <document>
                    <filename>budget-2026.pdf</filename>
                    <title>Budget 2026</title>
                    <datum>2026-01-19</datum>
                    <tags>
                        <tag>finance</tag>
                        <tag>2026</tag>
                    </tags>
                    <summary>Summary</summary>
                    <content>Full content</content>
                </document>
                """;
        Path file = INBOX.resolve("budget-2026.xml");
        Files.writeString(file, xml);

        scheduler.runImport();

        List<DocumentEntity> docs = documentRepository.findAll();
        assertEquals(1, docs.size());

        DocumentEntity doc = docs.get(0);
        assertEquals("Budget 2026", doc.getTitle());
        assertEquals("budget-2026.pdf", doc.getFilename());
        assertEquals("Full content", doc.getContent());
        assertEquals("Summary", doc.getSummary());
        assertEquals("xml-import", doc.getUsername());
        assertEquals("finance,2026", doc.getTags());
        assertEquals(LocalDate.of(2026, 1, 19), doc.getDocumentDate());
        assertEquals("OK", doc.getSummaryStatus());

        assertFalse(Files.exists(file));
        assertTrue(Files.exists(ARCHIVE.resolve("budget-2026.xml")));
    }

    private static Path createTempDir() {
        try {
            return Files.createTempDirectory("xmlimport-e2e-");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create temp directory for XML import tests", e);
        }
    }
}
