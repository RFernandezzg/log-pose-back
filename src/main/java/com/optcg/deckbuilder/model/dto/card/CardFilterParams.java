package com.optcg.deckbuilder.model.dto.card;

import lombok.Data;

@Data
public class CardFilterParams {
    private String color;
    private String type;
    private String cost;
    private String power;
    private String set;
    private String attribute;
    private String name;
}
