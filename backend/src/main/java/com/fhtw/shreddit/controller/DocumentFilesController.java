package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.api.dto.OcrFileRequestDto;
import com.fhtw.shreddit.service.DocumentService;
import com.fhtw.shreddit.service.RabbitMQService;
import com.fhtw.shreddit.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final DocumentService documentService;

    @Value("${MINIO_BUCKET:documents}")
    private String bucket;

    public DocumentFilesController(StorageService storageService, RabbitMQService rabbitMQService, DocumentService documentService) {
        this.storageService = storageService;
        this.rabbitMQService = rabbitMQService;
        this.documentService = documentService;
    }

    @PostMapping(value = "/documents/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "title", required = false) String title) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        // Upload file with user information
        String objectName = storageService.upload(file, username);

        // Notify OCR worker via RabbitMQ
        OcrFileRequestDto msg = new OcrFileRequestDto(bucket, objectName, username);
        rabbitMQService.sendOcrFileRequest(msg);

        // Create a document in the database
        String effectiveTitle = (title != null && !title.isBlank()) ? title : (file.getOriginalFilename() != null ? file.getOriginalFilename() : objectName);
        DocumentDto documentDto = new DocumentDto();
        documentDto.setTitle(effectiveTitle);
        documentDto.setContent(""); // Will be populated by OCR worker
        documentDto.setUsername(username);
        DocumentDto createdDoc = documentService.create(documentDto);

        // Build a JSON object similar to RootController contract so frontend can res.json()
        Map<String, Object> response = new HashMap<>();
        response.put("id", createdDoc.getId());
        response.put("title", createdDoc.getTitle());
        response.put("createdAt", createdDoc.getCreatedAt().toString());
        response.put("filename", objectName);
        response.put("objectName", objectName);
        response.put("bucket", bucket);
        response.put("uploadedBy", username);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/documents/download/{name}")
    public ResponseEntity<InputStreamResource> download(@PathVariable("name") String name) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        // Get the uploader of the file
        String uploader = storageService.getUploader(name);

        // Check if the current user is the uploader or if the uploader is null (for backward compatibility)
        if (uploader == null || uploader.equals(username)) {
            return storageService.download(name);
        } else {
            log.warn("User '{}' attempted to download file '{}' uploaded by '{}'", username, name, uploader);
            return ResponseEntity.status(403).build(); // Forbidden
        }
    }
}
