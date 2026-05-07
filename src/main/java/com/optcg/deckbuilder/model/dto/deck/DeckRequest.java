package com.optcg.deckbuilder.model.dto.deck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class DeckRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    private String description;

    @NotBlank(message = "Leader card ID is required")
    private String leaderCardId;

    private String leaderColor;

    @NotNull(message = "Visibility flag is required")
    private Boolean isPublic;

    // Card ID -> Quantity
    @NotNull(message = "Cards map is required")
    @NotEmpty(message = "Deck must contain at least one card")
    private Map<@NotBlank(message = "Card ID cannot be blank") String,
                @NotNull(message = "Card quantity is required") Integer> cards;
}
