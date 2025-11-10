package com.fhtw.shreddit.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class StorageService {
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final MinioClient minioClient;

    @Value("${MINIO_BUCKET:documents}")
    private String bucket;

    public StorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String upload(MultipartFile file) {
        return upload(file, "anonymous");
    }

    public String upload(MultipartFile file, String username) {
        String objectName = Objects.requireNonNullElse(file.getOriginalFilename(), file.getName());
        String contentType = file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        // Create user metadata
        Map<String, String> userMetadata = new HashMap<>();
        userMetadata.put("uploaded-by", username);

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(contentType)
                            .userMetadata(userMetadata)
                            .build()
            );
            log.info("Uploaded '{}' to bucket '{}' by user '{}'", objectName, bucket, username);
            return objectName;
        } catch (Exception e) {
            log.error("Failed to upload to MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public ResponseEntity<InputStreamResource> download(String name) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(name).build());
            // We don't know exact type; serve as octet-stream and suggest filename
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + name + "\"")
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            log.error("Failed to download from MinIO: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}
