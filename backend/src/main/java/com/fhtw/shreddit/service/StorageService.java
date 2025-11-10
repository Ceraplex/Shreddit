package com.fhtw.shreddit.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
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

    /**
     * Get the username of the user who uploaded the file
     * @param name The name of the file
     * @return The username of the uploader, or null if not found
     */
    public String getUploader(String name) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(name).build());
            String uploader = stat.userMetadata().get("uploaded-by");
            if (uploader == null) {
                log.warn("No uploader metadata found for file: {}", name);
                // For backward compatibility, allow access to files without metadata
                return null;
            }
            return uploader;
        } catch (Exception e) {
            log.error("Failed to get metadata from MinIO: {}", e.getMessage(), e);
            // For backward compatibility, allow access to files that can't be stat'd
            return null;
        }
    }

    public ResponseEntity<InputStreamResource> download(String name) {
        try {
            log.info("Attempting to download file '{}' from bucket '{}'", name, bucket);

            // Check if the object exists first
            try {
                minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(name).build());
            } catch (Exception e) {
                log.error("File '{}' not found in bucket '{}': {}", name, bucket, e.getMessage());
                return ResponseEntity.notFound().build();
            }

            // Get the object
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(name).build());

            log.info("Successfully retrieved file '{}' from bucket '{}'", name, bucket);

            // We don't know exact type; serve as octet-stream and suggest filename
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + name + "\"")
                    .body(new InputStreamResource(stream));
        } catch (Exception e) {
            log.error("Failed to download file '{}' from bucket '{}': {}", name, bucket, e.getMessage(), e);
            return ResponseEntity.status(500).build(); // Internal Server Error
        }
    }
}
