package com.fhtw.xmlimportworker.service;

import com.fhtw.xmlimportworker.model.ImportedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class XmlDocumentParser {
    private static final Logger log = LoggerFactory.getLogger(XmlDocumentParser.class);

    public ImportedDocument parse(Path file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        Document document = factory.newDocumentBuilder().parse(file.toFile());
        Element root = document.getDocumentElement();
        if (root == null) {
            throw new IllegalArgumentException("Missing root element");
        }
        if (!"document".equals(root.getTagName())) {
            throw new IllegalArgumentException("Unexpected root element: " + root.getTagName());
        }

        String title = text(root, "title").orElse(null);
        String filename = text(root, "filename").orElse(null);
        String content = text(root, "content").orElse(null);
        String summary = text(root, "summary").orElse(null);
        String username = text(root, "username").orElse("xml-import");
        LocalDate documentDate = text(root, "date")
                .or(() -> text(root, "datum"))
                .map(LocalDate::parse)
                .orElse(null);

        List<String> tags = new ArrayList<>();
        NodeList tagNodes = root.getElementsByTagName("tag");
        for (int i = 0; i < tagNodes.getLength(); i++) {
            String tag = tagNodes.item(i).getTextContent();
            if (tag != null && !tag.isBlank()) {
                tags.add(tag.trim());
            }
        }
        if (tags.isEmpty()) {
            text(root, "tags")
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .ifPresent(raw -> {
                        String[] parts = raw.split(",");
                        for (String part : parts) {
                            String value = part.trim();
                            if (!value.isBlank()) {
                                tags.add(value);
                            }
                        }
                    });
        }

        if ((title == null || title.isBlank()) && filename != null) {
            title = filename;
        }

        if ((title == null || title.isBlank()) && (filename == null || filename.isBlank())) {
            log.warn("XML document without title/filename in {}", file.getFileName());
        }

        return new ImportedDocument(title, filename, content, summary, username, documentDate, tags);
    }

    private Optional<String> text(Element root, String tag) {
        NodeList nodes = root.getElementsByTagName(tag);
        if (nodes == null || nodes.getLength() == 0) {
            return Optional.empty();
        }
        String value = nodes.item(0).getTextContent();
        if (value == null) {
            return Optional.empty();
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? Optional.empty() : Optional.of(trimmed);
    }
}
