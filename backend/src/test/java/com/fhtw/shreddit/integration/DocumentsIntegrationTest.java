package com.fhtw.shreddit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fhtw.shreddit.controller.AuthController.AuthRequest;
import com.fhtw.shreddit.repository.UserRepository;
import com.fhtw.shreddit.service.RabbitMQService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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

    @MockBean
    private RabbitMQService rabbitMQService;

    private final String TEST_USERNAME = "docintegrationuser";
    private final String TEST_PASSWORD = "testpassword123";
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Configure the RabbitMQ mock to do nothing when sendOcrRequest is called
        doNothing().when(rabbitMQService).sendOcrRequest(any());

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
        // Clean up test user
        userRepository.findByUsername(TEST_USERNAME)
                .ifPresent(user -> userRepository.delete(user));
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
