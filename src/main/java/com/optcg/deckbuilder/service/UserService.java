package com.optcg.deckbuilder.service;

import com.optcg.deckbuilder.model.dto.user.UpdateProfileRequest;
import com.optcg.deckbuilder.model.dto.user.UserProfileDto;
import com.optcg.deckbuilder.model.entity.User;
import com.optcg.deckbuilder.exception.ConflictException;
import com.optcg.deckbuilder.exception.NotFoundException;
import com.optcg.deckbuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final TotpService totpService;

    public UserProfileDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return mapToDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new ConflictException("Invalid current password");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public UserProfileDto updateAvatar(String username, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > 2 * 1024 * 1024) { // 2MB
            throw new IllegalArgumentException("File size exceeds 2MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        try {
            String avatarUrl = cloudinaryService.uploadFileWithHash(file, "avatars");
            user.setAvatarUrl(avatarUrl);
            User updatedUser = userRepository.save(user);
            return mapToDto(updatedUser);
        } catch (IOException e) {
            throw new RuntimeException("Error uploading avatar", e);
        }
    }

    @Transactional
    public void enableTotp(String username, String secret, int code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isTotpEnabled()) {
            throw new ConflictException("TOTP is already enabled");
        }

        if (!totpService.validateCode(secret, code)) {
            throw new IllegalArgumentException("Invalid TOTP code");
        }

        user.setTotpSecret(secret);
        user.setIsTotpEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void disableTotp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setTotpSecret(null);
        user.setIsTotpEnabled(false);
        userRepository.save(user);
    }

    private UserProfileDto mapToDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .isTotpEnabled(user.isTotpEnabled())
                .build();
    }
}
