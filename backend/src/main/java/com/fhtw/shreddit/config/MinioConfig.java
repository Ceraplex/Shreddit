package com.fhtw.shreddit.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${MINIO_ENDPOINT:http://localhost:9000}")
    private String endpoint;

    @Value("${MINIO_ACCESS_KEY:minioadmin}")
    private String accessKey;

    @Value("${MINIO_SECRET_KEY:minioadmin}")
    private String secretKey;

    @Value("${MINIO_BUCKET:documents}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        log.info("Initializing MinIO client with endpoint: {}", endpoint);

        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        // Ensure bucket exists with multiple retries
        int maxRetries = 5;
        int retryDelayMs = 2000; // 2 seconds

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Checking if MinIO bucket '{}' exists (attempt {}/{})", bucket, attempt, maxRetries);
                boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());

                if (!exists) {
                    log.info("Creating MinIO bucket: {}", bucket);
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                    log.info("Successfully created MinIO bucket: {}", bucket);
                } else {
                    log.info("MinIO bucket '{}' already exists", bucket);
                }

                // If we get here, the bucket exists and we can break out of the retry loop
                break;
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    log.warn("Failed to verify/create MinIO bucket '{}' (attempt {}/{}): {}", 
                            bucket, attempt, maxRetries, e.getMessage());
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted while waiting to retry MinIO bucket creation");
                    }
                } else {
                    log.error("Failed to verify/create MinIO bucket '{}' after {} attempts: {}", 
                            bucket, maxRetries, e.getMessage(), e);
                }
            }
        }

        return client;
    }
}
