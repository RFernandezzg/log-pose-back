package com.optcg.deckbuilder.controller;

import com.optcg.deckbuilder.model.dto.collection.CollectionItemDto;
import com.optcg.deckbuilder.model.dto.collection.UpdateCollectionQuantityRequest;
import com.optcg.deckbuilder.security.UserDetailsImpl;
import com.optcg.deckbuilder.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collection")
@Validated
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping
    public ResponseEntity<List<CollectionItemDto>> getMyCollection(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(collectionService.getMyCollection(userDetails.getUsername()));
    }

    @PostMapping("/{cardId}")
    public ResponseEntity<CollectionItemDto> addCardToCollection(
            @PathVariable String cardId,
            @RequestParam(defaultValue = "1") @jakarta.validation.constraints.Min(value = 1, message = "Quantity must be at least 1") Integer quantity,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(collectionService.addCardToCollection(cardId, quantity, userDetails.getUsername()));
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CollectionItemDto> updateCardQuantity(
            @PathVariable String cardId,
            @Valid @RequestBody UpdateCollectionQuantityRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CollectionItemDto response = collectionService.updateCardQuantity(cardId, request.getQuantity(), userDetails.getUsername());
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> removeCardFromCollection(
            @PathVariable String cardId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        collectionService.removeCardFromCollection(cardId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
