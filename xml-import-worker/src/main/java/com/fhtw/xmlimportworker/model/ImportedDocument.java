package com.fhtw.xmlimportworker.model;

import java.time.LocalDate;
import java.util.List;

public record ImportedDocument(
        String title,
        String filename,
        String content,
        String summary,
        String username,
        LocalDate documentDate,
        List<String> tags
) {
}
