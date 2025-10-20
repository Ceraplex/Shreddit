package com.fhtw.shreddit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_USERNAME = "integrationtestuser";
    private final String TEST_PASSWORD = "testpassword123";

    @BeforeEach
    void setUp() {
        // Clean up any existing test user before each test
        userRepository.findByUsername(TEST_USERNAME)
                .ifPresent(user -> userRepository.delete(user));
    }

    @AfterEach
    void tearDown() {
        // Clean up test user after each test
        userRepository.findByUsername(TEST_USERNAME)
                .ifPresent(user -> userRepository.delete(user));
    }

    @Test
    void testFullAuthenticationFlow() throws Exception {
        // 1. Register a new user
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.username = TEST_USERNAME;
        registerRequest.password = TEST_PASSWORD;

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME))
                .andReturn();

        // Extract token from registration response
        String registerResponseJson = registerResult.getResponse().getContentAsString();
        Map<String, String> registerResponse = objectMapper.readValue(registerResponseJson, Map.class);
        String token = registerResponse.get("token");

        assertNotNull(token, "Token should not be null");
        assertTrue(token.length() > 0, "Token should not be empty");

        // 2. Verify user exists in database
        assertTrue(userRepository.existsByUsername(TEST_USERNAME), "User should exist in database after registration");

        // 3. Access the /me endpoint with the token
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));

        // 4. Login with the same credentials
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.username = TEST_USERNAME;
        loginRequest.password = TEST_PASSWORD;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));

        // 5. Try to access /me without a token
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        // 6. Try to register with the same username again
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("username already exists"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        // 1. Register a new user
        AuthRequest registerRequest = new AuthRequest();
        registerRequest.username = TEST_USERNAME;
        registerRequest.password = TEST_PASSWORD;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Try to login with wrong password
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.username = TEST_USERNAME;
        loginRequest.password = "wrongpassword";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid credentials"));

        // 3. Try to login with non-existent username
        loginRequest.username = "nonexistentuser";
        loginRequest.password = TEST_PASSWORD;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid credentials"));
    }
}