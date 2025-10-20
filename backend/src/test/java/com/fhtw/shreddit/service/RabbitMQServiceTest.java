package com.fhtw.shreddit.service;

import com.fhtw.shreddit.api.dto.OcrRequestDto;
import com.fhtw.shreddit.exception.MessagePublishException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMQService rabbitMQService;

    private OcrRequestDto ocrRequest;
    private final String queueName = "test.queue.ocr";

    @BeforeEach
    void setUp() {
        // Use reflection to set the queueName field in the service
        try {
            java.lang.reflect.Field field = RabbitMQService.class.getDeclaredField("ocrQueueName");
            field.setAccessible(true);
            field.set(rabbitMQService, queueName);
        } catch (Exception e) {
            fail("Failed to set ocrQueueName field: " + e.getMessage());
        }

        // Create test OCR request
        ocrRequest = new OcrRequestDto(1L, "test.pdf");
    }

    @Test
    void sendOcrRequestSendsMessageToQueue() {
        // Arrange
        doNothing().when(rabbitTemplate).convertAndSend(eq(queueName), eq(ocrRequest));

        // Act - should not throw exception
        assertDoesNotThrow(() -> rabbitMQService.sendOcrRequest(ocrRequest));

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(queueName, ocrRequest);
    }

    @Test
    void sendOcrRequestThrowsExceptionWhenRabbitTemplateThrows() {
        // Arrange
        AmqpException amqpException = new AmqpException("Test exception");
        doThrow(amqpException).when(rabbitTemplate).convertAndSend(anyString(), any(OcrRequestDto.class));

        // Act & Assert
        MessagePublishException exception = assertThrows(
            MessagePublishException.class,
            () -> rabbitMQService.sendOcrRequest(ocrRequest)
        );

        // Verify the exception has the correct message and cause
        assertEquals("Failed to send OCR request", exception.getMessage());
        assertSame(amqpException, exception.getCause());

        verify(rabbitTemplate, times(1)).convertAndSend(queueName, ocrRequest);
    }

    @Test
    void sendOcrRequestHandlesNullRequest() {
        // Arrange
        OcrRequestDto nullRequest = null;

        // Act & Assert
        assertThrows(
            NullPointerException.class,
            () -> rabbitMQService.sendOcrRequest(nullRequest)
        );

        // Verify rabbitTemplate was never called
        verify(rabbitTemplate, never()).convertAndSend(eq(queueName), any(OcrRequestDto.class));
    }
}
