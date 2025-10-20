package com.fhtw.shreddit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fhtw.shreddit.api.dto.DocumentDto;
import com.fhtw.shreddit.controller.AuthController.AuthRequest;
import com.fhtw.shreddit.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
public class DocumentCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_USERNAME = "doccrudtestuser";
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
    void testDocumentCrudOperations() throws Exception {
        // 1. Create a document
        DocumentDto newDocument = new DocumentDto(null, "CRUD Test Document", "Content for CRUD test", null);

        MvcResult createResult = mockMvc.perform(post("/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDocument))
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("CRUD Test Document"))
                .andReturn();

        // Extract document ID from response
        String createResponseJson = createResult.getResponse().getContentAsString();
        DocumentDto createdDocument = objectMapper.readValue(createResponseJson, DocumentDto.class);
        createdDocumentId = createdDocument.getId();

        // 2. Get the document by ID
        mockMvc.perform(get("/documents/" + createdDocumentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdDocumentId))
                .andExpect(jsonPath("$.title").value("CRUD Test Document"))
                .andExpect(jsonPath("$.content").value("Content for CRUD test"));

        // 3. Get all documents and verify the created document is in the list
        mockMvc.perform(get("/documents")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + createdDocumentId + ")]").exists());

        // 4. Delete the document
        mockMvc.perform(delete("/documents/" + createdDocumentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // 5. Verify the document was deleted
        mockMvc.perform(get("/documents/" + createdDocumentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());

        // Set createdDocumentId to null so tearDown doesn't try to delete it again
        createdDocumentId = null;
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        // Try to access documents without authentication
        mockMvc.perform(get("/documents"))
                .andExpect(status().isUnauthorized());

        // Try to create a document without authentication
        DocumentDto newDocument = new DocumentDto(null, "Unauthorized Test", "This should fail", null);
        mockMvc.perform(post("/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDocument)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDocumentNotFound() throws Exception {
        // Try to get a non-existent document
        mockMvc.perform(get("/documents/99999")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}