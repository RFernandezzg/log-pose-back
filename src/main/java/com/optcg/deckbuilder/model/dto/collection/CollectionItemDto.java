package com.optcg.deckbuilder.model.dto.collection;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollectionItemDto {
    private Long id;

    private String cardId;

    private Integer quantity;
}
