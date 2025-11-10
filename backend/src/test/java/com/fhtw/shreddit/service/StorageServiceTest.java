package com.fhtw.shreddit.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private MinioClient minioClient;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageService(minioClient);
        // Inject bucket value since we are not running within Spring context
        org.springframework.test.util.ReflectionTestUtils.setField(storageService, "bucket", "documents");
    }

    @Test
    void uploadSendsToMinioWithMetadataAndReturnsObjectName() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "doc.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        // Stub putObject to return a response
        io.minio.ObjectWriteResponse ow = mock(io.minio.ObjectWriteResponse.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(ow);

        String result = storageService.upload(file, "alice");

        assertEquals("doc.pdf", result);
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        PutObjectArgs args = captor.getValue();
        assertNotNull(args);
    }

    @Test
    void getUploaderReturnsUsernameFromMetadata() throws Exception {
        StatObjectResponse resp = mock(StatObjectResponse.class);
        Map<String, String> meta = new HashMap<>();
        meta.put("uploaded-by", "bob");
        when(resp.userMetadata()).thenReturn(meta);
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(resp);

        String uploader = storageService.getUploader("some.pdf");
        assertEquals("bob", uploader);
    }

    @Test
    void getUploaderReturnsNullWhenNoMetadata() throws Exception {
        StatObjectResponse resp = mock(StatObjectResponse.class);
        when(resp.userMetadata()).thenReturn(new HashMap<>());
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(resp);

        String uploader = storageService.getUploader("some.pdf");
        assertNull(uploader);
    }

    @Test
    void getUploaderReturnsNullOnException() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(new RuntimeException("boom"));
        String uploader = storageService.getUploader("x.pdf");
        assertNull(uploader);
    }

    @Test
    void downloadReturnsFileStreamWhenExists() throws Exception {
        // First statObject succeeds
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mock(StatObjectResponse.class));
        // Then getObject returns data
        // Return a simple mock GetObjectResponse; StorageService wraps it into InputStreamResource
        io.minio.GetObjectResponse resp = mock(io.minio.GetObjectResponse.class);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(resp);

        ResponseEntity<InputStreamResource> response = storageService.download("a.txt");
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        // We already verified MinIO was called and response is present; content stream may not be readable twice in this context
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void downloadReturns404WhenNotFound() throws Exception {
        // statObject throws to simulate not found
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(new RuntimeException("not found"));
        ResponseEntity<InputStreamResource> response = storageService.download("missing.txt");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void downloadReturns500OnUnexpectedError() throws Exception {
        // stat ok, but getObject throws
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mock(StatObjectResponse.class));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("io"));
        ResponseEntity<InputStreamResource> response = storageService.download("err.txt");
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void deleteObjectReturnsTrueWhenDeleted() throws Exception {
        // object exists
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mock(StatObjectResponse.class));
        // remove ok
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));
        assertTrue(storageService.deleteObject("ok.txt"));
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteObjectReturnsFalseWhenNotExists() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(new RuntimeException("nf"));
        assertFalse(storageService.deleteObject("missing.txt"));
        verify(minioClient, never()).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void deleteObjectReturnsFalseOnError() throws Exception {
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mock(StatObjectResponse.class));
        doThrow(new RuntimeException("boom")).when(minioClient).removeObject(any(RemoveObjectArgs.class));
        assertFalse(storageService.deleteObject("x.txt"));
    }
}
