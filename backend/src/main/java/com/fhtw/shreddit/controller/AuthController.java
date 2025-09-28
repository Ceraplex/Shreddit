package com.fhtw.shreddit.controller;

import com.fhtw.shreddit.model.UserEntity;
import com.fhtw.shreddit.repository.UserRepository;
import com.fhtw.shreddit.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {
        log.info("POST /auth/register username={}", (req != null ? req.username : "<null>"));
        if (req == null || isBlank(req.username) || isBlank(req.password)) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }
        if (users.existsByUsername(req.username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        UserEntity u = new UserEntity();
        u.setUsername(req.username.trim());
        u.setPasswordHash(passwordEncoder.encode(req.password));
        users.save(u);
        String token = jwtService.generate(u.getUsername());
        return ResponseEntity.ok(Map.of("token", token, "username", u.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        log.info("POST /auth/login username={}", (req != null ? req.username : "<null>"));
        if (req == null || isBlank(req.username) || isBlank(req.password)) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password required"));
        }
        return users.findByUsername(req.username.trim())
                .filter(u -> passwordEncoder.matches(req.password, u.getPasswordHash()))
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(Map.of("token", jwtService.generate(u.getUsername()), "username", u.getUsername())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid credentials")));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "unauthenticated"));
        }
        String username = String.valueOf(auth.getPrincipal());
        return ResponseEntity.ok(Map.of("username", username));
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public static class AuthRequest {
        public String username;
        public String password;
    }
}
