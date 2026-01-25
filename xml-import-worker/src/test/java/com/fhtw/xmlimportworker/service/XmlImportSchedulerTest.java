package com.fhtw.xmlimportworker.service;

import com.fhtw.xmlimportworker.config.XmlImportProperties;
import com.fhtw.xmlimportworker.model.DocumentEntity;
import com.fhtw.xmlimportworker.model.ImportedDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XmlImportSchedulerTest {

    @TempDir
    Path tempDir;

    @Mock
    private XmlDocumentParser parser;

    @Mock
    private XmlDocumentImporter importer;

    @Test
    void processesXmlFilesAndArchivesThem() throws Exception {
        Path inbox = tempDir.resolve("inbox");
        Path archive = tempDir.resolve("archive");
        Files.createDirectories(inbox);
        Files.createDirectories(archive);

        Path xmlOne = inbox.resolve("one.xml");
        Path xmlTwo = inbox.resolve("two.xml");
        Path ignore = inbox.resolve("ignore.txt");
        Files.writeString(xmlOne, "<document></document>");
        Files.writeString(xmlTwo, "<document></document>");
        Files.writeString(ignore, "ignored");

        XmlImportProperties props = new XmlImportProperties();
        props.setInputDir(inbox.toString());
        props.setArchiveDir(archive.toString());
        props.setFilePattern("*.xml");

        ImportedDocument parsed = new ImportedDocument(
                "Title",
                "file.pdf",
                null,
                null,
                "xml-import",
                LocalDate.of(2026, 1, 19),
                List.of("tag")
        );

        when(parser.parse(any(Path.class))).thenReturn(parsed);
        when(importer.importDocument(any(ImportedDocument.class))).thenReturn(new DocumentEntity());

        XmlImportScheduler scheduler = new XmlImportScheduler(props, parser, importer);
        scheduler.runImport();

        verify(parser, times(2)).parse(any(Path.class));
        verify(importer, times(2)).importDocument(any(ImportedDocument.class));

        assertFalse(Files.exists(xmlOne));
        assertFalse(Files.exists(xmlTwo));
        assertTrue(Files.exists(archive.resolve("one.xml")));
        assertTrue(Files.exists(archive.resolve("two.xml")));
        assertTrue(Files.exists(ignore));
    }

    @Test
    void deletesFilesWhenArchiveIsNotConfigured() throws Exception {
        Path inbox = tempDir.resolve("inbox-delete");
        Files.createDirectories(inbox);

        Path xml = inbox.resolve("to-delete.xml");
        Files.writeString(xml, "<document></document>");

        XmlImportProperties props = new XmlImportProperties();
        props.setInputDir(inbox.toString());
        props.setArchiveDir("");
        props.setFilePattern("*.xml");

        when(parser.parse(any(Path.class))).thenReturn(new ImportedDocument(
                "Title",
                "file.pdf",
                null,
                null,
                "xml-import",
                null,
                List.of()
        ));
        when(importer.importDocument(any(ImportedDocument.class))).thenReturn(new DocumentEntity());

        XmlImportScheduler scheduler = new XmlImportScheduler(props, parser, importer);
        scheduler.runImport();

        verify(parser, times(1)).parse(any(Path.class));
        verify(importer, times(1)).importDocument(any(ImportedDocument.class));
        assertFalse(Files.exists(xml));
    }
}
