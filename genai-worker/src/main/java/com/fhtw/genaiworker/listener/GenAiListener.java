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
        if (docId == null || ocrPath == null || ocrPath.isBlank()) {
            log.warn("GENAI: received malformed message: docId={}, ocrPath={}", docId, ocrPath);
            return;
        }
        log.info("GENAI: job received for docId={} object={}", docId, ocrPath);

        try {
            String ocrText;
            log.info("GENAI: loading OCR from MinIO bucket='{}' object='{}' for docId={}", bucket, ocrPath, docId);
            try (InputStream in = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(ocrPath).build())) {
                ocrText = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (ocrText.isBlank()) {
                throw new IllegalStateException("OCR text is empty for docId=" + docId);
            }
            log.info("GENAI: OCR text loaded from MinIO ({} chars) for docId={}", ocrText.length(), docId);

            String summary;
            try {
                log.info("GENAI: calling Gemini for docId={}", docId);
                summary = geminiClient.summarizeGerman(ocrText);
            } catch (Exception first) {
                log.warn("GENAI: Gemini call failed once for docId={}, retrying. Error: {}", docId, first.getMessage());
                summary = geminiClient.summarizeGerman(ocrText);
            }
            if (summary == null || summary.isBlank()) {
                throw new IllegalStateException("Gemini returned an empty summary for docId=" + docId);
            }
            log.info("GENAI: Gemini summary length={} for docId={}", summary.length(), docId);

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
            log.info("GENAI: summary stored in MinIO object='{}' for docId={}", summaryObject, docId);

            DocumentEntity entity = documentRepository.findById(docId)
                    .orElseThrow(() -> new IllegalStateException("Document not found: " + docId));
            entity.setOcrText(ocrText);
            entity.setSummary(summary);
            entity.setSummaryStatus("OK");
            documentRepository.save(entity);

            log.info("GENAI: summary stored in DB and MinIO for docId={}", docId);
        } catch (Exception e) {
            log.error("GENAI: error while processing docId={}: {}", docId, e.getMessage(), e);
            try {
                documentRepository.findById(docId).ifPresent(entity -> {
                    entity.setSummary(null);
                    entity.setSummaryStatus("FAILED");
                    documentRepository.save(entity);
                });
            } catch (Exception dbEx) {
                log.error("GENAI: failed to mark summary_status=FAILED for docId={}", docId, dbEx);
            }
        }
    }
}

