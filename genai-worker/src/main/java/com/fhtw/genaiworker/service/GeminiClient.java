package com.fhtw.genaiworker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {
    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    @Value("${GEMINI_API_KEY:}")
    private String apiKey;

    @Value("${GEMINI_MODEL:gemini-2.0-flash}")
    private String model;

    @Value("${GEMINI_TEMPERATURE:0.2}")
    private double temperature;

    @Value("${GEMINI_MAX_TOKENS:512}")
    private int maxTokens;

    private final RestTemplate rest;

    public GeminiClient(RestTemplateBuilder builder) {
        this.rest = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String summarizeGerman(String ocrText) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not set");
        }
        if (ocrText == null || ocrText.isBlank()) {
            throw new IllegalArgumentException("OCR text is empty");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        Map<String, Object> systemInstruction = Map.of(
                "parts", List.of(Map.of("text", "Please summarize the following OCR text in 3-5 neutral sentences in German. Do not invent information that is not present in the text."))
        );

        Map<String, Object> contents = Map.of(
                "parts", List.of(Map.of("text", ocrText))
        );

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", temperature);
        generationConfig.put("maxOutputTokens", maxTokens);

        Map<String, Object> body = new HashMap<>();
        body.put("systemInstruction", systemInstruction);
        body.put("contents", List.of(contents));
        body.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = rest.postForEntity(url, entity, Map.class);
            Map resp = response.getBody();
            if (resp == null) {
                throw new RestClientException("Null response from Gemini");
            }
            Object candidates = resp.get("candidates");
            if (candidates instanceof List<?> list && !list.isEmpty()) {
                Object c0 = list.get(0);
                if (c0 instanceof Map<?, ?> cm) {
                    Object content = cm.get("content");
                    if (content instanceof Map<?, ?> cont) {
                        Object parts = cont.get("parts");
                        if (parts instanceof List<?> plist && !plist.isEmpty()) {
                            Object p0 = plist.get(0);
                            if (p0 instanceof Map<?, ?> pm) {
                                Object text = pm.get("text");
                                if (text != null) {
                                    String summary = String.valueOf(text);
                                    if (summary.isBlank()) {
                                        throw new RestClientException("Gemini returned an empty summary");
                                    }
                                    return summary;
                                }
                            }
                        }
                    }
                }
            }
            throw new RestClientException("Unexpected Gemini response structure; status=" + response.getStatusCode());
        } catch (HttpStatusCodeException e) {
            log.error("Gemini API HTTP {} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        } catch (RestClientException e) {
            log.error("Gemini API error: {}", e.getMessage(), e);
            throw e;
        }
    }
}

