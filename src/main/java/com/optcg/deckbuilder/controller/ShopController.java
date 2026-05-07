package com.optcg.deckbuilder.controller;

import com.optcg.deckbuilder.model.dto.shop.OrderRequest;
import com.optcg.deckbuilder.model.dto.shop.OrderResponse;
import com.optcg.deckbuilder.model.dto.shop.ShopItemDto;
import com.optcg.deckbuilder.security.UserDetailsImpl;
import com.optcg.deckbuilder.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/items")
    public ResponseEntity<List<ShopItemDto>> getShopItems() {
        return ResponseEntity.ok(shopService.getAllItems());
    }

    @GetMapping("/orders/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(shopService.getMyOrders(userDetails.getUsername()));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(shopService.createOrder(userDetails.getUsername(), request));
    }
}
