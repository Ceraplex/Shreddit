package com.fhtw.indexingworker.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.fhtw.indexingworker.dto.IndexingRequestDto;
import com.fhtw.indexingworker.model.DocumentEntity;
import com.fhtw.indexingworker.repo.DocumentRepository;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexingServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private DocumentRepository documentRepository;

    private IndexingService indexingService;

    @BeforeEach
    void setup() {
        indexingService = new IndexingService(minioClient, elasticsearchClient, documentRepository, "documents");
    }

    @Test
    void indexDocumentStoresPayloadInElasticsearch() throws Exception {
        IndexingRequestDto dto = new IndexingRequestDto(1L, "documents", "documents/1/ocr.txt");

        DocumentEntity entity = new DocumentEntity();
        entity.setId(1L);
        entity.setTitle("Test");
        entity.setContent("content");
        entity.setUsername("tester");
        entity.setSummary("summary");
        when(documentRepository.findById(1L)).thenReturn(Optional.of(entity));

        ByteArrayInputStream bais = new ByteArrayInputStream("hello elastic".getBytes(StandardCharsets.UTF_8));
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.read()).thenAnswer(invocation -> bais.read());
        when(response.read(any(byte[].class))).thenAnswer(invocation -> bais.read(invocation.getArgument(0)));
        when(response.read(any(byte[].class), anyInt(), anyInt()))
                .thenAnswer(invocation -> bais.read(invocation.getArgument(0), invocation.getArgument(1), invocation.getArgument(2)));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        IndexResponse indexResponse = mock(IndexResponse.class);
        when(elasticsearchClient.index(any(IndexRequest.class))).thenReturn(indexResponse);

        indexingService.indexDocument(dto);

        verify(minioClient).getObject(any(GetObjectArgs.class));
        verify(elasticsearchClient).index(any(IndexRequest.class));
    }

    @Test
    void indexDocumentSkipsWhenDocumentMissing() throws Exception {
        IndexingRequestDto dto = new IndexingRequestDto(99L, "documents", "documents/99/ocr.txt");
        when(documentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> indexingService.indexDocument(dto));
        verify(elasticsearchClient, never()).index(any(IndexRequest.class));
    }

    @Test
    void indexDocumentNoopOnNullRequest() {
        indexingService.indexDocument(null);
        verifyNoInteractions(minioClient, elasticsearchClient, documentRepository);
    }
}
