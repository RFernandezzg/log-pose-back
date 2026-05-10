package com.optcg.deckbuilder.external;

import com.optcg.deckbuilder.model.dto.card.ExternalCardDto;
import com.optcg.deckbuilder.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.time.Duration;

/**
 * Cliente para consumir la API externa de cartas de One Piece TCG.
 * Actúa como proxy transparente hacia la API de optcgcards.
 *
 * Responsabilidades:
 * - Construir URIs correctas con parámetros de filtrado
 * - Manejar errores de conectividad de forma clara
 * - Registrar llamadas para debugging
 *
 * NO es responsable de:
 * - Cachear datos (se maneja en CardService)
 * - Filtrado local (delegado a la API)
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class OptcgApiClient {

    private final WebClient webClient;

    @Value("${optcg.api.base-url}")
    private String baseUrl;

    // Timeout para llamadas a la API externa (segundos)
    private static final long REQUEST_TIMEOUT_SECONDS = 10;

    public List<ExternalCardDto> getAllCards() {
        String uri = baseUrl + "/api/allSetCards/";
        log.debug("Fetching all cards from: {}", uri);
        return fetchExternalCards(uri);
    }

    public ExternalCardDto getCardById(String id) {
        String cardSetId = id.contains("_") ? id.split("_")[0] : id;
        String uri = baseUrl + "/api/sets/card/" + cardSetId + "/";
        log.debug("Fetching card by card_set_id from: {}", uri);
        return fetchSingle(uri, id);
    }

    public List<ExternalCardDto> getCardVersionsBySetId(String cardSetId) {
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/sets/card/{cardSetId}/")
                .buildAndExpand(cardSetId)
                .toUriString();
        log.debug("Fetching card versions for set {} from: {}", cardSetId, uri);
        return fetchExternalCards(uri);
    }

    public List<Map<String, String>> getSets() {
        String uri = baseUrl + "/api/allSets/";
        log.debug("Fetching sets from: {}", uri);
        return fetchList(uri, new ParameterizedTypeReference<List<Map<String, String>>>() {
        });
    }

    /**
     * Busca cartas con filtros específicos.
     * Si no hay filtros, delega a getAllCards() (que usa /api/allSetCards/).
     * Si hay al menos un filtro, usa /api/sets/filtered/ con los parámetros.
     *
     * @param name      Nombre de la carta (búsqueda parcial)
     * @param color     Color de la carta
     * @param type      Tipo de carta
     * @param cost      Costo de la carta
     * @param power     Poder de la carta
     * @param set       ID del set
     * @param attribute Atributo de la carta
     * @return Lista de cartas que coinciden con los criterios
     */
    public List<ExternalCardDto> getFilteredCards(String name, String color, String type, String cost,
            String power, String set, String attribute) {
        // Si no hay parámetros, devolver todas las cartas
        if (isAllBlank(name, color, type, cost, power, set, attribute)) {
            return getAllCards();
        }

        // Si hay parámetros, usar el endpoint de filtrado
        String uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/api/sets/filtered/")
                .queryParamIfPresent("card_name", valueIfHasText(name))
                .queryParamIfPresent("color", valueIfHasText(color))
                .queryParamIfPresent("type", valueIfHasText(type))
                .queryParamIfPresent("cost", valueIfHasText(cost))
                .queryParamIfPresent("power", valueIfHasText(power))
                .queryParamIfPresent("set_id", valueIfHasText(set))
                .queryParamIfPresent("attribute", valueIfHasText(attribute))
                .toUriString();

        log.debug("Fetching filtered cards from: {}", uri);
        return fetchExternalCards(uri);
    }

    private List<ExternalCardDto> fetchExternalCards(String uri) {
        try {
            log.debug("GET request to (raw): {}", uri);
            List<Map<String, Object>> raw = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    })
                    .block(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS));

            if (raw == null)
                return List.of();

            return raw.stream()
                    .map(this::mapCard)
                    .toList();
        } catch (Exception ex) {
            log.error("Error fetching raw cards from API at {}: {}", uri, ex.getMessage(), ex);
            // Return empty list as a graceful fallback to avoid propagating 502 to clients
            return List.of();
        }
    }

    private ExternalCardDto fetchSingle(String uri, String originalId) {
        try {
            log.debug("GET request to: {}", uri);
            List<Map<String, Object>> raw = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    })
                    .block(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS));

            if (raw == null || raw.isEmpty()) {
                return null;
            }

            for (Map<String, Object> m : raw) {
                Object cardImageIdObj = m.get("card_image_id");
                Object cardSetIdObj = m.get("card_set_id");
                String currentId = cardImageIdObj != null ? cardImageIdObj.toString()
                        : (cardSetIdObj != null ? cardSetIdObj.toString() : null);
                if (originalId.equals(currentId)) {
                    return mapCard(m);
                }
            }

            return mapCard(raw.get(0));
        } catch (Exception ex) {
            log.error("Error fetching card from API at {}: {}. Response: {}", uri, ex.getClass().getSimpleName(),
                    ex.getMessage());
            // Return null when a single fetch fails; controller/service can handle nulls
            return null;
        }
    }

    private ExternalCardDto mapCard(Map<String, Object> m) {
        ExternalCardDto c = new ExternalCardDto();

        Object cardImageIdObj = m.get("card_image_id");
        Object cardSetIdObj = m.get("card_set_id");

        c.setId(cardImageIdObj != null ? cardImageIdObj.toString()
                : (cardSetIdObj != null ? cardSetIdObj.toString() : null));
        c.setCardImageId(cardImageIdObj != null ? cardImageIdObj.toString() : null);
        c.setCardSetId(cardSetIdObj != null ? cardSetIdObj.toString() : null);

        Object cardName = m.get("card_name");
        c.setName(cardName != null ? cardName.toString() : null);

        Object cardType = m.get("card_type");
        c.setType(cardType != null ? cardType.toString() : null);

        Object attribute = m.get("attribute");
        c.setAttribute(attribute != null ? attribute.toString() : null);

        Object cardPower = m.get("card_power");
        c.setPower(cardPower != null ? cardPower.toString() : null);

        Object counter = m.get("counter_amount");
        c.setCounter(counter != null ? counter.toString() : null);

        Object cardColor = m.get("card_color");
        c.setColor(cardColor != null ? cardColor.toString() : null);

        Object cardText = m.get("card_text");
        c.setText(cardText != null ? cardText.toString() : null);

        Object cardCost = m.get("card_cost");
        c.setCost(cardCost != null ? cardCost.toString() : null);

        Object life = m.get("life");
        c.setLife(life != null ? life.toString() : null);

        Object cardImage = m.get("card_image");
        c.setImageUrl(cardImage != null ? cardImage.toString() : null);

        Object setName = m.get("set_name");
        c.setSetName(setName != null ? setName.toString() : null);

        Object setId = m.get("set_id");
        c.setSetId(setId != null ? setId.toString() : null);

        Object subTypes = m.get("sub_types");
        c.setSubTypes(subTypes != null ? subTypes.toString() : null);

        Object rarity = m.get("rarity");
        c.setRarity(rarity != null ? rarity.toString() : null);

        return c;
    }

    private <T> T fetchList(String uri, ParameterizedTypeReference<T> responseType) {
        try {
            log.debug("GET request to: {}", uri);
            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS));
        } catch (Exception ex) {
            log.error("Error fetching cards from API at {}: {}. Response: {}", uri, ex.getClass().getSimpleName(),
                    ex.getMessage());
            // Return an empty list as a safe fallback for list endpoints
            @SuppressWarnings("unchecked")
            T empty = (T) List.of();
            return empty;
        }
    }

    private java.util.Optional<String> valueIfHasText(String value) {
        return (value != null && !value.isBlank()) ? java.util.Optional.of(value) : java.util.Optional.empty();
    }

    private boolean isAllBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
