package com.optcg.deckbuilder.controller;

import com.optcg.deckbuilder.model.dto.auth.AuthResponse;
import com.optcg.deckbuilder.model.dto.auth.LoginRequest;
import com.optcg.deckbuilder.model.dto.auth.RegisterRequest;
import com.optcg.deckbuilder.security.UserDetailsImpl;
import com.optcg.deckbuilder.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        AuthResponse response = AuthResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .avatarUrl(userDetails.getAvatarUrl())
                // token is not returned again here
                .build();
                
        return ResponseEntity.ok(response);
    }
}
