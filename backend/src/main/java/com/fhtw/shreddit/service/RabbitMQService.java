package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.OcrRequestDto;
import com.fhtw.shreddit.exception.MessagePublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQService {
    private static final Logger log = LoggerFactory.getLogger(RabbitMQService.class);

    private final RabbitTemplate rabbitTemplate;
    private final String ocrQueueName;

    public RabbitMQService(RabbitTemplate rabbitTemplate, 
                          @Value("${rabbitmq.queue.ocr}") String ocrQueueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.ocrQueueName = ocrQueueName;
    }

    public void sendOcrRequest(OcrRequestDto request) {
        try {
            log.info("Sending OCR request for document ID: {}", request.getDocumentId());
            rabbitTemplate.convertAndSend(ocrQueueName, request);
            log.info("OCR request sent successfully for document ID: {}", request.getDocumentId());
        } catch (AmqpException e) {
            log.error("Failed to send OCR request for document ID: {}", request.getDocumentId(), e);
            throw new MessagePublishException("Failed to send OCR request", e);
        }
    }
}