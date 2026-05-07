package com.optcg.deckbuilder.service;

import com.optcg.deckbuilder.external.OptcgApiClient;
import com.optcg.deckbuilder.model.dto.card.CardFilterParams;
import com.optcg.deckbuilder.model.dto.card.ExternalCardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CardService {

    private final OptcgApiClient apiClient;

    /**
     * Obtiene todas las cartas disponibles.
     * Las cartas se cachean para mejorar rendimiento.
     */
    @Cacheable(value = "cards")
    public List<ExternalCardDto> getAllCards() {
        return apiClient.getAllCards();
    }

    /**
     * Obtiene una carta específica por su ID.
     * El resultado se cachea por ID para evitar llamadas repetidas.
     */
    @Cacheable(value = "card", key = "#id")
    public ExternalCardDto getCardById(String id) {
        return apiClient.getCardById(id);
    }

    /**
     * Obtiene todos los sets/expansiones disponibles.
     * Los sets se cachean ya que cambian con poca frecuencia.
     */
    @Cacheable(value = "sets")
    public List<Map<String, String>> getSets() {
        return apiClient.getSets();
    }

    @Cacheable(value = "cardVersions", key = "#cardSetId")
    public List<ExternalCardDto> getCardVersionsBySetId(String cardSetId) {
        return apiClient.getCardVersionsBySetId(cardSetId);
    }

    /**
     * Obtiene cartas filtradas según los criterios especificados.
     * Delega directamente a la API externa sin procesamiento local.
     */
    public List<ExternalCardDto> getFilteredCards(CardFilterParams params) {
        return apiClient.getFilteredCards(
                params.getName(),
                params.getColor(),
                params.getType(),
                params.getCost(),
                params.getPower(),
                params.getSet(),
                params.getAttribute()
        );
    }
}
