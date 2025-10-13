package com.fhtw.shreddit.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping(path = "/")
    public ResponseEntity<String> root() {
        // Simple health/info endpoint for the root path so reverse proxies don't get a 403/404
        return ResponseEntity.ok("Shreddit backend is up");
    }

    // Avoid 500 errors from browsers auto-requesting /favicon.ico
    @GetMapping(path = "/favicon.ico")
    public ResponseEntity<byte[]> favicon() {
        // Return an empty 204 No Content so the browser stops retrying
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .build();
    }
}
