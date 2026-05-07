package com.optcg.deckbuilder.model.dto.shop;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Map;

@Data
public class OrderRequest {
    
    // Item ID -> Quantity
    @NotEmpty(message = "Order must contain at least one item")
    private Map<@NotNull(message = "Item ID is required") Long,
                @Positive(message = "Quantity must be greater than zero") Integer> items;
}
