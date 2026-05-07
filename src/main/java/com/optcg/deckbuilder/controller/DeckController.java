package com.optcg.deckbuilder.controller;

import com.optcg.deckbuilder.model.dto.deck.DeckRequest;
import com.optcg.deckbuilder.model.dto.deck.DeckResponse;
import com.optcg.deckbuilder.model.dto.deck.DeckSummaryDto;
import com.optcg.deckbuilder.security.UserDetailsImpl;
import com.optcg.deckbuilder.service.DeckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping
    public ResponseEntity<Page<DeckSummaryDto>> getPublicDecks(
            Pageable pageable,
            @RequestParam(required = false) String leaderCardId,
            @RequestParam(required = false) String color) {
        return ResponseEntity.ok(deckService.getPublicDecks(pageable, leaderCardId, color));
    }

    @GetMapping("/my")
    public ResponseEntity<List<DeckSummaryDto>> getMyDecks(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(deckService.getMyDecks(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> getDeckById(@PathVariable Long id,
                                                    @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(deckService.getDeckById(id, username));
    }

    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(@Valid @RequestBody DeckRequest request,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(deckService.createDeck(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckResponse> updateDeck(@PathVariable Long id,
                                                   @Valid @RequestBody DeckRequest request,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(deckService.updateDeck(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        deckService.deleteDeck(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<java.util.Map<String, Object>> toggleLike(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(deckService.toggleLike(id, userDetails.getUsername()));
    }

    @GetMapping("/{id}/like")
    public ResponseEntity<java.util.Map<String, Object>> getLikeStatus(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(java.util.Map.of("liked", false));
        }
        return ResponseEntity.ok(deckService.getLikeStatus(id, userDetails.getUsername()));
    }
}
