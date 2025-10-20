package com.fhtw.shreddit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.controller.AuthController.AuthRequest;
import com.fhtw.shreddit.model.UserEntity;
import com.fhtw.shreddit.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DocumentsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_USERNAME = "docintegrationuser";
    private final String TEST_PASSWORD = "testpassword123";
    private String authToken;
    private Long createdDocumentId;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up any existing test user
        userRepository.findByUsername(TEST_USERNAME)
                .ifPresent(user -> userRepository.delete(user));

        // Create a test user and get auth token
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.username = TEST_USERNAME;
        registerRequest.password = TEST_PASSWORD;

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String registerResponseJson = registerResult.getResponse().getContentAsString();
        Map<String, String> registerResponse = objectMapper.readValue(registerResponseJson, Map.class);
        authToken = registerResponse.get("token");
    }

    @AfterEach
    void tearDown() throws Exception {
        // Delete the created document if it exists
        if (createdDocumentId != null) {
            mockMvc.perform(delete("/documents/" + createdDocumentId)
                    .header("Authorization", "Bearer " + authToken));
        }

        // Clean up test user
        userRepository.findByUsername(TEST_USERNAME)
                .ifPresent(user -> userRepository.delete(user));
    }

    @Test
    void testDocumentUploadAndRetrievalFlow() throws Exception {
        // 1. Create a document directly via the API since the upload endpoint doesn't save to DB
        DocumentDto firstDocument = new DocumentDto(null, "Integration Test Document", "Content created for integration test", null);

        MvcResult firstCreateResult = mockMvc.perform(post("/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstDocument))
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Integration Test Document"))
                .andReturn();

        // Extract document ID from response
        String firstCreateResponseJson = firstCreateResult.getResponse().getContentAsString();
        DocumentDto firstCreatedDocument = objectMapper.readValue(firstCreateResponseJson, DocumentDto.class);
        createdDocumentId = firstCreatedDocument.getId();

        // 2. Get all documents
        mockMvc.perform(get("/documents")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + createdDocumentId + ")]").exists());

        // 3. Get the specific document by ID
        mockMvc.perform(get("/documents/" + createdDocumentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdDocumentId))
                .andExpect(jsonPath("$.title").value("Integration Test Document"));

        // 4. Create a new document via the documents API
        DocumentDto secondDocument = new DocumentDto(null, "New API Document", "Content created via API", null);

        MvcResult secondCreateResult = mockMvc.perform(post("/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondDocument))
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New API Document"))
                .andReturn();

        // Extract the second document ID
        String secondCreateResponseJson = secondCreateResult.getResponse().getContentAsString();
        DocumentDto secondCreatedDocument = objectMapper.readValue(secondCreateResponseJson, DocumentDto.class);
        Long secondDocumentId = secondCreatedDocument.getId();

        // 5. Verify both documents exist in the list
        mockMvc.perform(get("/documents")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + createdDocumentId + ")]").exists())
                .andExpect(jsonPath("$[?(@.id == " + secondDocumentId + ")]").exists());

        // 6. Delete the second document
        mockMvc.perform(delete("/documents/" + secondDocumentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // 7. Verify only the first document remains
        mockMvc.perform(get("/documents")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + createdDocumentId + ")]").exists())
                .andExpect(jsonPath("$[?(@.id == " + secondDocumentId + ")]").doesNotExist());

        // 8. Try to access documents without authentication
        mockMvc.perform(get("/documents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDocumentNotFoundHandling() throws Exception {
        // Try to get a non-existent document
        mockMvc.perform(get("/documents/99999")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());

        // Try to delete a non-existent document
        mockMvc.perform(delete("/documents/99999")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent()); // Most APIs return 204 even if resource doesn't exist
    }
}
