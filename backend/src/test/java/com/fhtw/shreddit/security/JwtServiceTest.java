package com.fhtw.shreddit.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void generateAndValidateReturnsUsername() {
        JwtService jwt = new JwtService("unit-test-secret", 3600);
        String token = jwt.generate("alice");
        assertEquals("alice", jwt.validateAndGetUsername(token));
    }

    @Test
    void tamperedTokenFailsValidation() {
        JwtService jwt = new JwtService("unit-test-secret", 3600);
        String token = jwt.generate("bob");
        // Tamper by flipping a character safely within base64url string
        String tampered = token.substring(0, token.length() - 2) + "_" + token.substring(token.length() - 1);
        assertNull(jwt.validateAndGetUsername(tampered));
    }

    @Test
    void expiredTokenFailsValidation() {
        // Negative TTL guarantees exp < now
        JwtService jwt = new JwtService("unit-test-secret", -1);
        String token = jwt.generate("carol");
        assertNull(jwt.validateAndGetUsername(token));
    }

    @Test
    void invalidBase64TokenFailsValidation() {
        JwtService jwt = new JwtService("unit-test-secret", 3600);
        // Not a valid Base64 string
        String invalidToken = "not-a-valid-base64-token!@#$%^&*()";
        assertNull(jwt.validateAndGetUsername(invalidToken));
    }

    @Test
    void tokenWithWrongNumberOfPartsFailsValidation() {
        JwtService jwt = new JwtService("unit-test-secret", 3600);
        // Create a token with only 3 parts (missing signature)
        String username = "dave";
        long now = java.time.Instant.now().getEpochSecond();
        long exp = now + 3600;
        String payload = username + ":" + now + ":" + exp;
        String invalidToken = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertNull(jwt.validateAndGetUsername(invalidToken));
    }

    @Test
    void tokenWithNonNumericTimestampFailsValidation() {
        JwtService jwt = new JwtService("unit-test-secret", 3600);
        // Create a token with non-numeric timestamp
        String invalidPayload = "eve:not-a-number:not-a-number:fake-signature";
        String invalidToken = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString(invalidPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertNull(jwt.validateAndGetUsername(invalidToken));
    }
}
