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
}
