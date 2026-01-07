package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.model.DocumentEntity;
import com.fhtw.shreddit.repository.DocumentRepository;
import com.fhtw.shreddit.search.IndexedDocument;
import com.fhtw.shreddit.search.SearchGateway;
import com.fhtw.shreddit.search.SearchHit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private SearchGateway searchGateway;
    @Mock
    private DocumentRepository documentRepository;

    private SearchService searchService;

    @BeforeEach
    void setup() {
        searchService = new SearchService(searchGateway, documentRepository);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("alice", "pw"));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void searchReturnsMappedResultsForUser() {
        IndexedDocument indexed = new IndexedDocument();
        indexed.setTitle("Hello World");
        SearchHit hit = new SearchHit(1L, indexed, 1.0);
        when(searchGateway.search("hello")).thenReturn(List.of(hit));

        DocumentEntity entity = new DocumentEntity();
        entity.setId(1L);
        entity.setTitle("Hello World");
        entity.setContent("Content");
        entity.setUsername("alice");
        entity.setCreatedAt(LocalDateTime.now());
        when(documentRepository.findAllById(anyCollection())).thenReturn(List.of(entity));

        List<DocumentDto> results = searchService.search("hello");

        assertEquals(1, results.size());
        assertEquals(entity.getId(), results.get(0).getId());
        assertEquals(entity.getTitle(), results.get(0).getTitle());
    }

    @Test
    void searchFiltersOutForeignDocuments() {
        IndexedDocument indexed = new IndexedDocument();
        SearchHit hit = new SearchHit(2L, indexed, 1.0);
        when(searchGateway.search("secret")).thenReturn(List.of(hit));

        DocumentEntity entity = new DocumentEntity();
        entity.setId(2L);
        entity.setTitle("Secret");
        entity.setUsername("bob");
        entity.setCreatedAt(LocalDateTime.now());
        when(documentRepository.findAllById(anyCollection())).thenReturn(List.of(entity));

        List<DocumentDto> results = searchService.search("secret");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchFallsBackToDatabaseWhenEsEmpty() {
        when(searchGateway.search("offline")).thenReturn(List.of());

        DocumentEntity entity = new DocumentEntity();
        entity.setId(10L);
        entity.setTitle("Offline title");
        entity.setContent("Some content");
        entity.setUsername("alice");
        entity.setCreatedAt(LocalDateTime.now());
        when(documentRepository.findAll()).thenReturn(List.of(entity));

        List<DocumentDto> results = searchService.search("offline");

        assertEquals(1, results.size());
        assertEquals(entity.getId(), results.get(0).getId());
    }
}
