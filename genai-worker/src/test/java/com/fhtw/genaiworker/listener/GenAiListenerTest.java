package com.fhtw.genaiworker.listener;

import com.fhtw.genaiworker.dto.GenAiRequestDto;
import com.fhtw.genaiworker.model.DocumentEntity;
import com.fhtw.genaiworker.repo.DocumentRepository;
import com.fhtw.genaiworker.service.GeminiClient;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenAiListenerTest {

    @Mock
    private MinioClient minioClient;
    @Mock
    private GeminiClient geminiClient;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private GenAiListener listener;

    @BeforeEach
    void setup() {
        listener = new GenAiListener(minioClient, geminiClient, documentRepository, rabbitTemplate);
        ReflectionTestUtils.setField(listener, "bucket", "documents");
        ReflectionTestUtils.setField(listener, "indexingQueue", "indexing-queue");
    }

    @Test
    void handleGenaiRequestPublishesIndexingJob() throws Exception {
        GenAiRequestDto request = new GenAiRequestDto(1L, "documents/1/ocr.txt");

        ByteArrayInputStream ocrStream = new ByteArrayInputStream("sample ocr".getBytes(StandardCharsets.UTF_8));
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.read()).thenAnswer(invocation -> ocrStream.read());
        when(response.read(any(byte[].class))).thenAnswer(invocation -> ocrStream.read(invocation.getArgument(0)));
        when(response.read(any(byte[].class), anyInt(), anyInt()))
                .thenAnswer(invocation -> ocrStream.read(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2)));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        when(geminiClient.summarizeGerman("sample ocr")).thenReturn("summary text");

        DocumentEntity entity = new DocumentEntity();
        entity.setId(1L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(entity));

        listener.handleGenaiRequest(request);

        verify(documentRepository).save(any(DocumentEntity.class));
        verify(rabbitTemplate).convertAndSend(eq("indexing-queue"), any(Object.class));
    }

    @Test
    void handleQuotaFailureMarksFailedQuota() throws Exception {
        GenAiRequestDto request = new GenAiRequestDto(2L, "documents/2/ocr.txt");

        ByteArrayInputStream ocrStream = new ByteArrayInputStream("sample ocr".getBytes(StandardCharsets.UTF_8));
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.read()).thenAnswer(invocation -> ocrStream.read());
        when(response.read(any(byte[].class))).thenAnswer(invocation -> ocrStream.read(invocation.getArgument(0)));
        when(response.read(any(byte[].class), anyInt(), anyInt()))
                .thenAnswer(invocation -> ocrStream.read(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2)));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        when(geminiClient.summarizeGerman("sample ocr"))
                .thenThrow(HttpClientErrorException.create(HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", HttpHeaders.EMPTY, null, null));

        DocumentEntity entity = new DocumentEntity();
        entity.setId(2L);
        when(documentRepository.findById(2L)).thenReturn(Optional.of(entity));

        listener.handleGenaiRequest(request);

        verify(documentRepository, atLeastOnce()).save(argThat(saved ->
                "FAILED_QUOTA".equals(saved.getSummaryStatus())
                        && "Gemini quota exceeded; try again later".equals(saved.getSummary())
        ));
    }
}
