package com.fhtw.shreddit.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class JwtService {
    private final byte[] secret;
    private final long ttlSeconds;

    public JwtService(
            @Value("${security.jwt-secret:change-me-super-secret}") String secret,
            @Value("${security.jwt-ttl-seconds:86400}") long ttlSeconds
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
    }

    public String generate(String username) {
        long now = Instant.now().getEpochSecond();
        long exp = now + ttlSeconds;
        String payload = username + ":" + now + ":" + exp;
        String sig = hmac(payload);
        String token = payload + ":" + sig;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    public String validateAndGetUsername(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 4) return null;
            String username = parts[0];
            long iat = Long.parseLong(parts[1]);
            long exp = Long.parseLong(parts[2]);
            String sig = parts[3];
            String payload = parts[0] + ":" + parts[1] + ":" + parts[2];
            if (!hmac(payload).equals(sig)) return null;
            if (Instant.now().getEpochSecond() > exp) return null;
            return username;
        } catch (Exception e) {
            return null;
        }
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign token", e);
        }
    }
}
