package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.OcrRequestDto;
import com.fhtw.shreddit.service.RabbitMQService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class RootController {
    private static final Logger log = LoggerFactory.getLogger(RootController.class);

    private final RabbitMQService rabbitMQService;

    public RootController(RabbitMQService rabbitMQService) {
        this.rabbitMQService = rabbitMQService;
    }

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

    @PostMapping(path = "/upload")
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file, @RequestParam(value = "title", required = false) String title) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a file");
        }

        String filename = file.getOriginalFilename();
        log.info("Received PDF document: {}", filename);

        try {
            // Create OCR request with temporary ID and filename
            OcrRequestDto ocrRequest = new OcrRequestDto(-1L, filename);
            rabbitMQService.sendOcrRequest(ocrRequest);

            // Dummy response object für das Frontend
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", System.currentTimeMillis()); // Dummy-ID
            response.put("title", title != null ? title : filename);
            response.put("createdAt", java.time.Instant.now().toString());
            response.put("filename", filename);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing PDF document: {}", e.getMessage(), e);
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("error", "Error processing PDF document: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
