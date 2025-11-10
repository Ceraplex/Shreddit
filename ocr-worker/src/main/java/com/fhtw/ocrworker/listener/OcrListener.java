package com.fhtw.ocrworker.listener;

import com.fhtw.ocrworker.dto.OcrFileRequestDto;
import com.fhtw.ocrworker.service.OcrProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OcrListener {
    private static final Logger log = LoggerFactory.getLogger(OcrListener.class);

    private final OcrProcessorService ocrProcessorService;

    @Value("${rabbitmq.queue.ocr}")
    private String queueName;

    public OcrListener(OcrProcessorService ocrProcessorService) {
        this.ocrProcessorService = ocrProcessorService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.ocr}")
    public void onMessage(@Payload OcrFileRequestDto dto) {
        if (dto == null) {
            log.warn("Received null OCR message");
            return;
        }
        String bucket = dto.getBucket();
        String objectName = dto.getObjectName();
        String username = dto.getUsername() != null ? dto.getUsername() : "anonymous";

        log.info("Received OCR request: bucket={}, object={}, user={}", bucket, objectName, username);
        try {
            ocrProcessorService.processPdf(bucket, objectName);
            log.info("OCR processing completed: bucket={}, object={}, user={}", bucket, objectName, username);
        } catch (Exception e) {
            log.error("Error processing OCR for bucket={}, object={}, user={}", bucket, objectName, username, e);
        }
    }
}
