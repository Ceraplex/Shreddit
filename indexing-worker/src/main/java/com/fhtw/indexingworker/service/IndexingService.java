package com.fhtw.indexingworker.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.fhtw.indexingworker.dto.IndexingRequestDto;
import com.fhtw.indexingworker.model.DocumentEntity;
import com.fhtw.indexingworker.model.IndexedDocument;
import com.fhtw.indexingworker.repo.DocumentRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class IndexingService {
    private static final Logger log = LoggerFactory.getLogger(IndexingService.class);

    private final MinioClient minioClient;
    private final ElasticsearchClient elasticsearchClient;
    private final DocumentRepository documentRepository;
    private final String indexName;

    @Value("${MINIO_BUCKET:documents}")
    private String defaultBucket;

    public IndexingService(MinioClient minioClient,
                           ElasticsearchClient elasticsearchClient,
                           DocumentRepository documentRepository,
                           @Value("${elasticsearch.index:documents}") String indexName) {
        this.minioClient = minioClient;
        this.elasticsearchClient = elasticsearchClient;
        this.documentRepository = documentRepository;
        this.indexName = indexName;
    }

    public void indexDocument(IndexingRequestDto request) {
        if (request == null) {
            log.warn("INDEX: received null indexing request");
            return;
        }
        if (request.getDocumentId() == null || request.getObjectName() == null || request.getObjectName().isBlank()) {
            log.warn("INDEX: missing data in request {}", request);
            return;
        }
        long documentId = request.getDocumentId();
        String bucket = Optional.ofNullable(request.getBucket()).filter(b -> !b.isBlank()).orElse(defaultBucket);
        String objectName = request.getObjectName();

        try {
            String ocrText = readOcrText(bucket, objectName);

            DocumentEntity entity = documentRepository.findById(documentId)
                    .orElseThrow(() -> new IllegalStateException("Document not found for id=" + documentId));

            IndexedDocument indexedDocument = new IndexedDocument(
                    entity.getId(),
                    entity.getTitle(),
                    entity.getContent(),
                    entity.getSummary(),
                    (ocrText != null && !ocrText.isBlank()) ? ocrText : entity.getOcrText(),
                    entity.getUsername(),
                    entity.getCreatedAt()
            );

            IndexRequest<IndexedDocument> indexRequest = IndexRequest.of(i -> i
                    .index(indexName)
                    .id(String.valueOf(documentId))
                    .document(indexedDocument)
            );
            IndexResponse response = elasticsearchClient.index(indexRequest);
            log.info("INDEX: stored docId={} into index={} result={}", documentId, indexName, response.result());
        } catch (Exception e) {
            log.error("INDEX: failed indexing docId={} object={} bucket={}", documentId, objectName, bucket, e);
            throw new RuntimeException("Failed to index document " + documentId, e);
        }
    }

    private String readOcrText(String bucket, String objectName) throws Exception {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(objectName).build())) {
            byte[] bytes = stream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (MinioException e) {
            log.error("INDEX: MinIO error while reading {}/{}: {}", bucket, objectName, e.getMessage());
            throw e;
        }
    }
}
