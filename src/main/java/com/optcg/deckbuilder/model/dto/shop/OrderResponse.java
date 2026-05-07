package com.optcg.deckbuilder.model.dto.shop;

import com.optcg.deckbuilder.model.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdAt;
    
    // Item Name -> Quantity
    private Map<String, Integer> items;
}
