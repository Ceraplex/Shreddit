package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.OcrFileRequestDto;
import com.fhtw.shreddit.service.RabbitMQService;
import com.fhtw.shreddit.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DocumentFilesController {
    private static final Logger log = LoggerFactory.getLogger(DocumentFilesController.class);

    private final StorageService storageService;
    private final RabbitMQService rabbitMQService;

    @Value("${MINIO_BUCKET:documents}")
    private String bucket;

    public DocumentFilesController(StorageService storageService, RabbitMQService rabbitMQService) {
        this.storageService = storageService;
        this.rabbitMQService = rabbitMQService;
    }

    @PostMapping(value = "/documents/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "title", required = false) String title) {
        String objectName = storageService.upload(file);
        // Notify OCR worker via RabbitMQ
        OcrFileRequestDto msg = new OcrFileRequestDto(bucket, objectName);
        rabbitMQService.sendOcrFileRequest(msg);

        // Build a JSON object similar to RootController contract so frontend can res.json()
        Map<String, Object> response = new HashMap<>();
        response.put("id", System.currentTimeMillis());
        String effectiveTitle = (title != null && !title.isBlank()) ? title : (file.getOriginalFilename() != null ? file.getOriginalFilename() : objectName);
        response.put("title", effectiveTitle);
        response.put("createdAt", Instant.now().toString());
        response.put("filename", objectName);
        response.put("objectName", objectName);
        response.put("bucket", bucket);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/documents/download/{name}")
    public ResponseEntity<InputStreamResource> download(@PathVariable("name") String name) {
        return storageService.download(name);
    }
}
