package com.fhtw.shreddit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fhtw.shreddit.controller.AuthController.AuthRequest;
import com.fhtw.shreddit.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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
public class EndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_USERNAME = "e2etestuser";
    private final String TEST_PASSWORD = "e2etestpassword";

    @AfterEach
    void tearDown() {
        // Clean up test user after test
        userRepository.findByUsername(TEST_USERNAME)
                .ifPresent(user -> userRepository.delete(user));
    }

    @Test
    void testEndToEndAuthenticationFlow() throws Exception {
        // 1. Check if the backend is up
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(result -> assertEquals("Shreddit backend is up", result.getResponse().getContentAsString()));

        // 2. Register a new user
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

        // 3. Verify user identity with the /me endpoint
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));

        // 4. Try to access /me without authentication
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        // 5. Try to access /me with invalid token
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isUnauthorized());

        // 6. Log back in
        AuthRequest loginRequest = new AuthRequest();
        loginRequest.username = TEST_USERNAME;
        loginRequest.password = TEST_PASSWORD;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Extract new token
        String loginResponseJson = loginResult.getResponse().getContentAsString();
        Map<String, String> loginResponse = objectMapper.readValue(loginResponseJson, Map.class);
        String newToken = loginResponse.get("token");

        // 7. Verify we can access /me with new token
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));

        // 8. Try to login with wrong password
        AuthRequest wrongPasswordRequest = new AuthRequest();
        wrongPasswordRequest.username = TEST_USERNAME;
        wrongPasswordRequest.password = "wrongpassword";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid credentials"));

        // 9. Try to login with non-existent username
        AuthRequest wrongUsernameRequest = new AuthRequest();
        wrongUsernameRequest.username = "nonexistentuser";
        wrongUsernameRequest.password = TEST_PASSWORD;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongUsernameRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid credentials"));

        // 10. Try to register with the same username again
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("username already exists"));
    }
}
