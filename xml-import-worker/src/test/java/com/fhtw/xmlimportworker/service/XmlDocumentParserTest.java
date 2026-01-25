package com.fhtw.xmlimportworker.service;

import com.fhtw.xmlimportworker.model.ImportedDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XmlDocumentParserTest {

    @TempDir
    Path tempDir;

    @Test
    void parsesStructuredTagsAndDate() throws Exception {
        String xml = """
                <document>
                    <filename>report.pdf</filename>
                    <title>Quarterly Report</title>
                    <date>2026-01-19</date>
                    <tags>
                        <tag>finance</tag>
                        <tag> 2026 </tag>
                    </tags>
                    <summary>Short summary</summary>
                    <content>Full content</content>
                    <username>admin</username>
                </document>
                """;
        Path file = tempDir.resolve("report.xml");
        Files.writeString(file, xml);

        XmlDocumentParser parser = new XmlDocumentParser();
        ImportedDocument doc = parser.parse(file);

        assertEquals("Quarterly Report", doc.title());
        assertEquals("report.pdf", doc.filename());
        assertEquals("Full content", doc.content());
        assertEquals("Short summary", doc.summary());
        assertEquals("admin", doc.username());
        assertEquals(LocalDate.of(2026, 1, 19), doc.documentDate());
        assertEquals(List.of("finance", "2026"), doc.tags());
    }

    @Test
    void parsesCsvTagsAndFallsBackToFilenameForTitle() throws Exception {
        String xml = """
                <document>
                    <filename>scan-001.pdf</filename>
                    <datum>2026-01-20</datum>
                    <tags> alpha, beta , , gamma </tags>
                </document>
                """;
        Path file = tempDir.resolve("scan.xml");
        Files.writeString(file, xml);

        XmlDocumentParser parser = new XmlDocumentParser();
        ImportedDocument doc = parser.parse(file);

        assertEquals("scan-001.pdf", doc.title());
        assertEquals("scan-001.pdf", doc.filename());
        assertEquals("xml-import", doc.username());
        assertEquals(LocalDate.of(2026, 1, 20), doc.documentDate());
        assertEquals(List.of("alpha", "beta", "gamma"), doc.tags());
        assertNull(doc.summary());
        assertNull(doc.content());
    }

    @Test
    void rejectsUnexpectedRootElement() throws Exception {
        String xml = """
                <not-a-document>
                    <title>Wrong Root</title>
                </not-a-document>
                """;
        Path file = tempDir.resolve("bad.xml");
        Files.writeString(file, xml);

        XmlDocumentParser parser = new XmlDocumentParser();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parser.parse(file));
        assertTrue(ex.getMessage().contains("Unexpected root element"));
    }
}
