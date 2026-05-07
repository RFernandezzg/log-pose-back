package com.optcg.deckbuilder.model.dto.deck;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class DeckResponse {
    private Long id;
    private String name;
    private String description;
    private String leaderCardId;
    @com.fasterxml.jackson.annotation.JsonProperty("isPublic")
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long likesCount;
    private Long userId;
    private String username;
    private String avatarUrl;
    
    // Card ID -> Quantity
    private Map<String, Integer> cards;
}
