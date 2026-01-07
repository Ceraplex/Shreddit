package com.fhtw.indexingworker.listener;

import com.fhtw.indexingworker.dto.IndexingRequestDto;
import com.fhtw.indexingworker.service.IndexingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexingListenerTest {

    @Mock
    private IndexingService indexingService;

    private IndexingListener listener;

    @BeforeEach
    void setup() {
        listener = new IndexingListener(indexingService);
    }

    @Test
    void onMessageDelegatesToService() {
        IndexingRequestDto dto = new IndexingRequestDto(1L, "documents", "documents/1/ocr.txt");
        listener.onMessage(dto);
        verify(indexingService).indexDocument(dto);
    }

    @Test
    void onMessageIgnoresNull() {
        listener.onMessage(null);
        verifyNoInteractions(indexingService);
    }
}
