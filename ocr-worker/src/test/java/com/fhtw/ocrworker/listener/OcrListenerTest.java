package com.fhtw.ocrworker.listener;

import com.fhtw.ocrworker.dto.OcrFileRequestDto;
import com.fhtw.ocrworker.service.OcrProcessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcrListenerTest {

    @Mock
    private OcrProcessorService ocrProcessorService;

    private OcrListener ocrListener;

    @BeforeEach
    void setUp() {
        ocrListener = new OcrListener(ocrProcessorService);
    }

    @Test
    void testOnMessage_Success() throws Exception {
        // Create a test DTO
        OcrFileRequestDto dto = new OcrFileRequestDto("test-bucket", "test-document.pdf", "test-user");
        
        // Call the listener
        ocrListener.onMessage(dto);
        
        // Verify that the service was called with the correct parameters
        verify(ocrProcessorService).processPdf("test-bucket", "test-document.pdf");
    }

    @Test
    void testOnMessage_NullDto() {
        // Call the listener with null DTO
        ocrListener.onMessage(null);
        
        // Verify that the service was not called
        verifyNoInteractions(ocrProcessorService);
    }

    @Test
    void testOnMessage_ServiceThrowsException() throws Exception {
        // Create a test DTO
        OcrFileRequestDto dto = new OcrFileRequestDto("test-bucket", "test-document.pdf", "test-user");
        
        // Make the service throw an exception
        doThrow(new RuntimeException("Test exception")).when(ocrProcessorService).processPdf(anyString(), anyString());
        
        // Call the listener - it should catch the exception and not propagate it
        ocrListener.onMessage(dto);
        
        // Verify that the service was called
        verify(ocrProcessorService).processPdf("test-bucket", "test-document.pdf");
    }
}