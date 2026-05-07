package com.optcg.deckbuilder.model.dto.deck;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeckSummaryDto {
    private Long id;
    private String name;
    private String leaderCardId;
    private String leaderColor;
    private Long likesCount;
    private String username;
    private String avatarUrl;
    private LocalDateTime createdAt;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isPublic")
    private Boolean isPublic;
}
