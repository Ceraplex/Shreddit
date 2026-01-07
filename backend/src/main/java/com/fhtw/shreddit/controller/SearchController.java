package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {
    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentDto>> search(@RequestParam("q") String query) {
        log.debug("Search request q='{}'", query);
        return ResponseEntity.ok(searchService.search(query));
    }

    @GetMapping("/api/search")
    public ResponseEntity<List<DocumentDto>> searchApi(@RequestParam("q") String query) {
        return ResponseEntity.ok(searchService.search(query));
    }
}
