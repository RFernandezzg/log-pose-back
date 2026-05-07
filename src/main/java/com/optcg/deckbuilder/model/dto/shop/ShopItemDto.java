package com.optcg.deckbuilder.model.dto.shop;

import com.optcg.deckbuilder.model.enums.ItemCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ShopItemDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Integer stock;
    private ItemCategory category;
    private LocalDateTime createdAt;
}