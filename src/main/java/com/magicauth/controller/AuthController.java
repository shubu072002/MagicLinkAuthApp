package com.magicauth.controller;

import com.magicauth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/magic-link
     * Body: { "email": "user@example.com" }
     * Sends a magic link to the provided email.
     */
    @PostMapping("/magic-link")
    public ResponseEntity<Map<String, String>> requestMagicLink(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));
        }

        try {
            authService.sendMagicLink(email.trim().toLowerCase());
            return ResponseEntity.ok(Map.of("message", "Magic link sent! Check your inbox."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send email. Please try again."));
        }
    }

    /**
     * POST /api/auth/verify
     * Body: { "token": "<magic-token>" }
     * Verifies the magic token and returns a JWT.
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required."));
        }

        try {
            String jwt = authService.verifyMagicToken(token.trim());
            return ResponseEntity.ok(Map.of("jwt", jwt, "message", "Login successful!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/auth/me
     * Returns the currently authenticated user's info (requires JWT).
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of("email", email));
    }
}
