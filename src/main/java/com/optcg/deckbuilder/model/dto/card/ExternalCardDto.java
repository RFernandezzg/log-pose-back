package com.optcg.deckbuilder.model.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExternalCardDto {
    private String id; // unique id like OP01-025 or OP01-025_p1

    @JsonProperty("card_set_id")
    private String cardSetId; // base card id like OP01-025

    @JsonProperty("card_image_id")
    private String cardImageId; // unique version id like OP01-025 or OP01-025_p1

    @JsonProperty("card_name")
    private String name;

    @JsonProperty("card_type")
    private String type;

    private String attribute;

    @JsonProperty("card_power")
    private String power;

    @JsonProperty("counter_amount")
    private String counter;

    @JsonProperty("card_color")
    private String color;

    @JsonProperty("card_text")
    private String text;

    @JsonProperty("card_cost")
    private String cost;

    private String life;

    @JsonProperty("card_image")
    private String imageUrl;

    @JsonProperty("set_name")
    private String setName;

    @JsonProperty("set_id")
    private String setId;

    @JsonProperty("sub_types")
    private String subTypes;

    private String rarity;
}
