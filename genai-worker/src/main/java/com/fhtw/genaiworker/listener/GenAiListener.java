package com.fhtw.genaiworker.listener;

import com.fhtw.genaiworker.dto.GenAiRequestDto;
import com.fhtw.genaiworker.model.DocumentEntity;
import com.fhtw.genaiworker.repo.DocumentRepository;
import com.fhtw.genaiworker.service.GeminiClient;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class GenAiListener {
    private static final Logger log = LoggerFactory.getLogger(GenAiListener.class);

    private final MinioClient minioClient;
    private final GeminiClient geminiClient;
    private final DocumentRepository documentRepository;

    @Value("${MINIO_BUCKET:documents}")
    private String bucket;

    public GenAiListener(MinioClient minioClient, GeminiClient geminiClient, DocumentRepository documentRepository) {
        this.minioClient = minioClient;
        this.geminiClient = geminiClient;
        this.documentRepository = documentRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.genai}")
    public void handleGenaiRequest(GenAiRequestDto request) {
        if (request == null) {
            log.warn("Received null GenAI message");
            return;
        }
        Long docId = request.getDocumentId();
        String ocrPath = request.getOcrPath();
        log.info("GENAI: job received for docId={}", docId);

        try {
            // Load OCR text from MinIO
            String ocrText;
            log.info("GENAI: loading OCR from MinIO bucket='{}' object='{}' for docId={}", bucket, ocrPath, docId);
            try (InputStream in = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(ocrPath).build())) {
                ocrText = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            log.info("GENAI: OCR text loaded from MinIO for docId={}", docId);

            // Call Gemini with retry (1 retry for transient errors)
            String summary;
            try {
                log.info("GENAI: calling Gemini for docId={}", docId);
                summary = geminiClient.summarizeGerman(ocrText);
            } catch (Exception first) {
                log.warn("GENAI: Gemini call failed once for docId={}: {} — retrying once", docId, first.getMessage());
                // retry once
                summary = geminiClient.summarizeGerman(ocrText);
            }
            log.info("GENAI: Gemini summary length={} for docId={}", summary != null ? summary.length() : -1, docId);

            // Store summary to MinIO
            String summaryObject = "documents/" + docId + "/summary.txt";
            byte[] bytes = summary.getBytes(StandardCharsets.UTF_8);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(summaryObject)
                            .stream(new java.io.ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType("text/plain; charset=utf-8")
                            .build()
            );

            // Update DB (must exist)
            log.info("GENAI: writing summary + status=OK to DB for docId={}", docId);
            DocumentEntity entity = documentRepository.findById(docId)
                    .orElseThrow(() -> new RuntimeException("Document not found: " + docId));
            entity.setSummary(summary);
            entity.setSummaryStatus("OK");
            documentRepository.save(entity);

            log.info("GENAI: summary stored in DB and MinIO for docId={}", docId);
        } catch (Exception e) {
            log.error("GENAI: error while processing docId={}", docId, e);
            try {
                documentRepository.findById(docId).ifPresent(entity -> {
                    entity.setSummaryStatus("FAILED");
                    documentRepository.save(entity);
                });
            } catch (Exception dbEx) {
                log.error("GENAI: failed to mark summary_status=FAILED for docId={}", docId, dbEx);
            }
        }
    }
}
