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
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        // Ensure bucket exists
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                log.info("Creating MinIO bucket: {}", bucket);
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            log.warn("Failed to verify/create MinIO bucket '{}': {}", bucket, e.getMessage());
        }
        return client;
    }
}
