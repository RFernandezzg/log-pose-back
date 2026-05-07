package com.optcg.deckbuilder.controller;

import com.optcg.deckbuilder.model.dto.card.CardFilterParams;
import com.optcg.deckbuilder.model.dto.card.ExternalCardDto;
import com.optcg.deckbuilder.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<ExternalCardDto>> getCards(@RequestParam Map<String, String> queryParams) {
        CardFilterParams params = new CardFilterParams();
        params.setColor(queryParams.get("color"));
        params.setType(queryParams.get("type"));
        params.setCost(queryParams.get("cost"));
        params.setPower(queryParams.get("power"));
        params.setSet(queryParams.get("set"));
        params.setAttribute(queryParams.get("attribute"));
        params.setName(firstNonBlank(queryParams.get("name"), queryParams.get("card_name")));
        return ResponseEntity.ok(cardService.getFilteredCards(params));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalCardDto> getCardById(@PathVariable String id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @GetMapping("/sets")
    public ResponseEntity<List<Map<String, String>>> getSets() {
        return ResponseEntity.ok(cardService.getSets());
    }

    @GetMapping("/leaders")
    public ResponseEntity<List<ExternalCardDto>> getLeaders() {
        CardFilterParams params = new CardFilterParams();
        params.setType("Leader");
        return ResponseEntity.ok(cardService.getFilteredCards(params));
    }

    @GetMapping("/sets/{cardSetId}/versions")
    public ResponseEntity<List<ExternalCardDto>> getCardVersions(@PathVariable String cardSetId) {
        return ResponseEntity.ok(cardService.getCardVersionsBySetId(cardSetId));
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
