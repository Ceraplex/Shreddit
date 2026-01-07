package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    private DocumentDto doc;

    @BeforeEach
    void setup() {
        doc = new DocumentDto(1L, "Doc", "Content", LocalDateTime.now(), "alice");
    }

    @Test
    void searchReturnsResults() {
        when(searchService.search("Doc")).thenReturn(List.of(doc));

        ResponseEntity<List<DocumentDto>> response = searchController.search("Doc");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(doc.getId(), response.getBody().get(0).getId());
    }
}
