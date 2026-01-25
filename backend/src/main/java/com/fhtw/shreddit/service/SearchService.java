package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.model.DocumentEntity;
import com.fhtw.shreddit.repository.DocumentRepository;
import com.fhtw.shreddit.search.SearchGateway;
import com.fhtw.shreddit.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final SearchGateway searchGateway;
    private final DocumentRepository documentRepository;

    public SearchService(SearchGateway searchGateway, DocumentRepository documentRepository) {
        this.searchGateway = searchGateway;
        this.documentRepository = documentRepository;
    }

    public List<DocumentDto> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        List<DocumentDto> fromIndex = mapFromIndex(query, username);
        if (!fromIndex.isEmpty()) {
            return fromIndex;
        }

        // Fallback: simple DB contains search across title/content/summary/ocr_text
        List<DocumentDto> fallback = documentRepository.findAll().stream()
                .filter(doc -> doc.getUsername() == null || doc.getUsername().equals(username))
                .filter(doc -> matches(doc, query))
                .map(this::toDto)
                .toList();
        log.debug("SEARCH: ES returned empty; fallback found {} docs for query='{}'", fallback.size(), query);
        return fallback;
    }

    private List<DocumentDto> mapFromIndex(String query, String username) {
        List<SearchHit> hits = searchGateway.search(query);
        List<Long> ids = hits.stream()
                .map(SearchHit::id)
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Long, DocumentEntity> entityMap = documentRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(DocumentEntity::getId, Function.identity()));

        List<DocumentDto> results = new ArrayList<>();
        for (SearchHit hit : hits) {
            Long id = hit.id();
            if (id == null) {
                continue;
            }
            DocumentEntity entity = entityMap.get(id);
            if (entity == null) {
                continue;
            }
            if (entity.getUsername() != null && !entity.getUsername().equals(username)) {
                continue;
            }
            results.add(toDto(entity));
        }
        log.debug("SEARCH: returning {} documents for query='{}' via ES", results.size(), query);
        return results;
    }

    private boolean matches(DocumentEntity doc, String q) {
        String needle = q.toLowerCase();
        return contains(doc.getTitle(), needle)
                || contains(doc.getContent(), needle)
                || contains(doc.getSummary(), needle)
                || contains(doc.getOcrText(), needle)
                || contains(doc.getTags(), needle);
    }

    private boolean contains(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase().contains(needle);
    }

    private DocumentDto toDto(DocumentEntity entity) {
        DocumentDto dto = new DocumentDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUsername(entity.getUsername());
        dto.setFilename(entity.getFilename());
        dto.setSummary(entity.getSummary());
        dto.setSummaryStatus(entity.getSummaryStatus());
        dto.setOcrText(entity.getOcrText());
        return dto;
    }
}
