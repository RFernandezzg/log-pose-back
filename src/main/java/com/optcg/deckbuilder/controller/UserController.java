package com.optcg.deckbuilder.controller;

import com.optcg.deckbuilder.model.dto.user.UpdateProfileRequest;
import com.optcg.deckbuilder.model.dto.user.UserProfileDto;
import com.optcg.deckbuilder.model.dto.user.TotpSetupResponse;
import com.optcg.deckbuilder.model.dto.user.TotpEnableRequest;
import com.optcg.deckbuilder.security.UserDetailsImpl;
import com.optcg.deckbuilder.service.UserService;
import com.optcg.deckbuilder.service.TotpService;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TotpService totpService;

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserProfile(username));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileDto> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userService.updateAvatar(userDetails.getUsername(), file));
    }

    @GetMapping("/totp/setup")
    public ResponseEntity<TotpSetupResponse> setupTotp(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        GoogleAuthenticatorKey key = totpService.generateSecret();
        String qrCodeUrl = totpService.getQrCodeUrl(key, userDetails.getUsername());
        
        return ResponseEntity.ok(TotpSetupResponse.builder()
                .secret(key.getKey())
                .qrCodeUrl(qrCodeUrl)
                .build());
    }

    @PostMapping("/totp/enable")
    public ResponseEntity<Void> enableTotp(
            @Valid @RequestBody TotpEnableRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.enableTotp(userDetails.getUsername(), request.getSecret(), request.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/totp/disable")
    public ResponseEntity<Void> disableTotp(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.disableTotp(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
