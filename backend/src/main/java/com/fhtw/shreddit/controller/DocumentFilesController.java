package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.api.dto.OcrFileRequestDto;
import com.fhtw.shreddit.service.DocumentService;
import com.fhtw.shreddit.service.RabbitMQService;
import com.fhtw.shreddit.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
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

        // First create a document record (summary status PENDING)
        String effectiveTitle = (title != null && !title.isBlank()) ? title : (file.getOriginalFilename() != null ? file.getOriginalFilename() : "uploaded.pdf");
        DocumentDto documentDto = new DocumentDto();
        documentDto.setTitle(effectiveTitle);
        documentDto.setContent("");
        documentDto.setUsername(username);
        documentDto.setSummaryStatus("PENDING");
        DocumentDto createdDoc = documentService.createForUpload(documentDto);

        // Upload file with user information; store original file under its object name
        String objectName = storageService.upload(file, username);

        // Update the document with the stored filename
        createdDoc.setFilename(objectName);
        // Persist the filename update (without triggering OCR)
        documentService.updateFilename(createdDoc.getId(), objectName);

        // Notify OCR worker via RabbitMQ, include documentId for downstream processing
        OcrFileRequestDto msg = new OcrFileRequestDto(createdDoc.getId(), bucket, objectName, username);
        rabbitMQService.sendOcrFileRequest(msg);

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

    @GetMapping("/api/documents/{id}/summary/download")
    public ResponseEntity<?> downloadSummary(@PathVariable("id") Long id) {
        try {
            // Prefer DB content to avoid MinIO dependency for summary download
            return documentService.getById(id)
                    .map(doc -> {
                        String summary = doc.getSummary();
                        if (summary == null || summary.isBlank()) {
                            return ResponseEntity.status(404).body("No summary available");
                        }
                        byte[] bytes = summary.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        ByteArrayResource resource = new ByteArrayResource(bytes);
                        return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"summary-" + id + ".txt\"")
                                .contentType(MediaType.TEXT_PLAIN)
                                .contentLength(bytes.length)
                                .body(resource);
                    })
                    .orElseGet(() -> ResponseEntity.status(404).body("Document not found"));
        } catch (Exception ex) {
            log.error("Error while downloading summary for doc {}", id, ex);
            return ResponseEntity.internalServerError().body("Error downloading summary");
        }
    }

    @GetMapping("/documents/download/{name}")
    public ResponseEntity<InputStreamResource> download(@PathVariable("name") String name) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null ? auth.getName() : "anonymous");
            boolean isAuthenticated = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(username);

            log.info("Download request for file '{}' by user '{}' (authenticated={})", name, username, isAuthenticated);

            // Get the uploader of the file
            String uploader = storageService.getUploader(name);

            // Log the uploader information
            if (uploader == null) {
                log.info("No uploader metadata found for file '{}', allowing download", name);
            } else {
                log.info("File '{}' was uploaded by '{}'", name, uploader);
            }

            // Allow download if:
            // - No uploader metadata exists (backward compatibility), OR
            // - Request is unauthenticated (public download), OR
            // - Authenticated user is the uploader
            if (uploader == null || !isAuthenticated || uploader.equals(username)) {
                ResponseEntity<InputStreamResource> response = storageService.download(name);
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Download successful for file '{}' by user '{}'", name, username);
                } else {
                    log.warn("Download failed for file '{}' by user '{}' with status {}",
                            name, username, response.getStatusCode());
                }
                return response;
            } else {
                log.warn("User '{}' attempted to download file '{}' uploaded by '{}'", username, name, uploader);
                return ResponseEntity.status(403).build(); // Forbidden
            }
        } catch (Exception e) {
            log.error("Error during download of file '{}': {}", name, e.getMessage(), e);
            return ResponseEntity.status(500).build(); // Internal Server Error
        }
    }
}
