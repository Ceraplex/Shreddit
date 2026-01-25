package com.fhtw.xmlimportworker.service;

import com.fhtw.xmlimportworker.config.XmlImportProperties;
import com.fhtw.xmlimportworker.model.ImportedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class XmlImportScheduler {
    private static final Logger log = LoggerFactory.getLogger(XmlImportScheduler.class);

    private final XmlImportProperties properties;
    private final XmlDocumentParser parser;
    private final XmlDocumentImporter importer;

    public XmlImportScheduler(XmlImportProperties properties, XmlDocumentParser parser, XmlDocumentImporter importer) {
        this.properties = properties;
        this.parser = parser;
        this.importer = importer;
    }

    @Scheduled(cron = "${xmlimport.cron}")
    public void runImport() {
        Path inputDir = Paths.get(properties.getInputDir());
        String pattern = properties.getFilePattern();
        if (pattern == null || pattern.isBlank()) {
            pattern = "*.xml";
        }

        try {
            Files.createDirectories(inputDir);
        } catch (IOException e) {
            log.error("XMLIMPORT: unable to create input directory {}", inputDir, e);
            return;
        }

        Path archiveDir = null;
        boolean deleteProcessed = true;
        if (properties.getArchiveDir() != null && !properties.getArchiveDir().isBlank()) {
            archiveDir = Paths.get(properties.getArchiveDir());
            try {
                Files.createDirectories(archiveDir);
                deleteProcessed = false;
            } catch (IOException e) {
                log.error("XMLIMPORT: unable to create archive directory {}", archiveDir, e);
                return;
            }
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir, pattern)) {
            for (Path file : stream) {
                if (Files.isDirectory(file)) {
                    continue;
                }
                handleFile(file, archiveDir, deleteProcessed);
            }
        } catch (IOException e) {
            log.error("XMLIMPORT: failed to scan input directory {}", inputDir, e);
        }
    }

    private void handleFile(Path file, Path archiveDir, boolean deleteProcessed) {
        try {
            ImportedDocument doc = parser.parse(file);
            importer.importDocument(doc);
            archiveOrDelete(file, archiveDir, deleteProcessed);
        } catch (Exception e) {
            log.error("XMLIMPORT: failed to import {}", file.getFileName(), e);
        }
    }

    private void archiveOrDelete(Path file, Path archiveDir, boolean deleteProcessed) throws IOException {
        if (deleteProcessed) {
            Files.deleteIfExists(file);
            log.info("XMLIMPORT: deleted processed file {}", file.getFileName());
            return;
        }

        Path target = archiveDir.resolve(file.getFileName());
        if (Files.exists(target)) {
            String stamped = file.getFileName().toString() + ".processed-" + System.currentTimeMillis();
            target = archiveDir.resolve(stamped);
        }
        Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("XMLIMPORT: archived {} to {}", file.getFileName(), target.getFileName());
    }
}
