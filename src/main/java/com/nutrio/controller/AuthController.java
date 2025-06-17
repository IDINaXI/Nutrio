package com.nutrio.controller;

import com.nutrio.dto.AuthResponse;
import com.nutrio.dto.LoginRequest;
import com.nutrio.dto.RegisterRequest;
import com.nutrio.dto.UpdateProfileRequest;
import com.nutrio.model.User;
import com.nutrio.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
@CrossOrigin(
    originPatterns = {
        "http://172.20.10.2:[*]",
        "http://localhost:[*]",
        "http://127.0.0.1:[*]",
        "http://192.168.100.112:[*]"
    },
    allowedHeaders = {"Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"},
    allowCredentials = "true"
)
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        String token = authService.register(request);
        User user = authService.getUserFromToken(token);
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        User user = authService.getUserFromToken(token);
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @GetMapping("/user")
    @Operation(summary = "Get current user data")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.getCurrentUser(token.substring(7)));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileRequest request) {
        User updatedUser = authService.updateProfile(token.substring(7), request);
        return ResponseEntity.ok(new AuthResponse(token.substring(7), updatedUser));
    }
}