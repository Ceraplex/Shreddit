package com.fhtw.shreddit.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ErrorControllerAdvice implements ErrorController {
    private static final Logger log = LoggerFactory.getLogger(ErrorControllerAdvice.class);

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Object statusAttr = request.getAttribute("jakarta.servlet.error.status_code");
        int status = statusAttr instanceof Integer ? (Integer) statusAttr : 500;
        String method = request.getMethod();
        String uri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        if (uri == null) uri = request.getRequestURI();
        log.warn("Error {} for {} {}", status, method, uri);
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("error", HttpStatus.valueOf(status).getReasonPhrase());
        body.put("path", uri);
        body.put("method", method);
        body.put("message", "Not found. Use /api/... from the UI (POST for login/register).");
        return ResponseEntity.status(status).body(body);
    }
}