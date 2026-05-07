package com.optcg.deckbuilder.model.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private LocalDateTime createdAt;
    @com.fasterxml.jackson.annotation.JsonProperty("isTotpEnabled")
    private boolean isTotpEnabled;
}
