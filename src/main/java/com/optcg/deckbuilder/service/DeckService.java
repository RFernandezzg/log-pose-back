package com.optcg.deckbuilder.service;

import com.optcg.deckbuilder.model.dto.deck.DeckRequest;
import com.optcg.deckbuilder.model.dto.deck.DeckResponse;
import com.optcg.deckbuilder.model.dto.deck.DeckSummaryDto;
import com.optcg.deckbuilder.model.entity.Deck;
import com.optcg.deckbuilder.model.entity.DeckCard;
import com.optcg.deckbuilder.model.entity.DeckLike;
import com.optcg.deckbuilder.model.entity.User;
import com.optcg.deckbuilder.exception.BadRequestException;
import com.optcg.deckbuilder.exception.ForbiddenException;
import com.optcg.deckbuilder.exception.NotFoundException;
import com.optcg.deckbuilder.model.dto.card.ExternalCardDto;
import com.optcg.deckbuilder.repository.DeckCardRepository;
import com.optcg.deckbuilder.repository.DeckLikeRepository;
import com.optcg.deckbuilder.repository.DeckRepository;
import com.optcg.deckbuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final DeckCardRepository deckCardRepository;
    private final DeckLikeRepository deckLikeRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final CardService cardService;

    public Page<DeckSummaryDto> getPublicDecks(Pageable pageable, String leaderCardId, String leaderColor) {
        return deckRepository.findByIsPublicTrueWithFilters(leaderCardId, leaderColor, pageable)
                .map(this::mapToSummaryDto);
    }

    public List<DeckSummaryDto> getMyDecks(String username) {
        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new NotFoundException("User not found"));

        return deckRepository.findByUser(user).stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DeckResponse getDeckById(Long id, String requesterUsername) {
        Deck deck = deckRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Deck not found"));

        if (!deck.isPublic()) {
            boolean isOwner = requesterUsername != null && deck.getUser().getUsername().equals(requesterUsername);
            if (!isOwner) {
                throw new ForbiddenException("This deck is private");
            }
                }

        List<DeckCard> cards = deckCardRepository.findByDeckId(deck.getId());
        Map<String, Integer> cardMap = cards.stream()
                .collect(Collectors.toMap(DeckCard::getCardId, DeckCard::getQuantity));

        return mapToResponseDto(deck, cardMap);
    }

    @Transactional
    public DeckResponse createDeck(DeckRequest request, String username) {
        validateDeckRules(request);

        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new NotFoundException("User not found"));

        Deck deck = Deck.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .leaderCardId(request.getLeaderCardId())
                .leaderColor(request.getLeaderColor())
                .isPublic(request.getIsPublic())
                .likesCount(0)
                .build();

        Deck savedDeck = deckRepository.save(deck);

        saveDeckCards(savedDeck, request.getCards());

        return mapToResponseDto(savedDeck, request.getCards());
    }

    @Transactional
    public DeckResponse updateDeck(Long id, DeckRequest request, String username) {
        validateDeckRules(request);

        Deck deck = deckRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Deck not found"));

        if (!deck.getUser().getUsername().equals(username)) {
                        throw new ForbiddenException("You don't have permission to edit this deck");
        }

        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setLeaderCardId(request.getLeaderCardId());
        deck.setLeaderColor(request.getLeaderColor());
        deck.setPublic(request.getIsPublic());

        Deck updatedDeck = deckRepository.save(deck);

        deckCardRepository.deleteByDeckId(updatedDeck.getId());
        entityManager.flush(); // Force deletion to be executed before insertion
        saveDeckCards(updatedDeck, request.getCards());

        return mapToResponseDto(updatedDeck, request.getCards());
    }

    @Transactional
    public void deleteDeck(Long id, String username) {
        Deck deck = deckRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Deck not found"));

        if (!deck.getUser().getUsername().equals(username)) {
                        throw new ForbiddenException("You don't have permission to delete this deck");
        }

        deckLikeRepository.deleteByDeck(deck);
        deckRepository.delete(deck);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long deckId, String username) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new NotFoundException("Deck not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        final boolean[] liked = {false};
        deckLikeRepository.findByDeckAndUser(deck, user).ifPresentOrElse(
                like -> {
                    deckLikeRepository.delete(like);
                    deck.setLikesCount(deck.getLikesCount() - 1);
                    liked[0] = false;
                },
                () -> {
                    DeckLike newLike = DeckLike.builder()
                            .deck(deck)
                            .user(user)
                            .build();
                    deckLikeRepository.save(newLike);
                    deck.setLikesCount(deck.getLikesCount() + 1);
                    liked[0] = true;
                }
        );

        deckRepository.save(deck);
        return Map.of("liked", liked[0], "likesCount", deck.getLikesCount());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLikeStatus(Long deckId, String username) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new NotFoundException("Deck not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        boolean liked = deckLikeRepository.existsByDeckAndUser(deck, user);
        return Map.of("liked", liked, "likesCount", deck.getLikesCount());
    }

    private void saveDeckCards(Deck deck, Map<String, Integer> cards) {
        if (cards != null && !cards.isEmpty()) {
            List<DeckCard> deckCards = cards.entrySet().stream()
                    .map(entry -> DeckCard.builder()
                            .deck(deck)
                            .cardId(entry.getKey())
                            .quantity(entry.getValue())
                            .build())
                    .collect(Collectors.toList());
            deckCardRepository.saveAll(deckCards);
        }
    }

    private DeckSummaryDto mapToSummaryDto(Deck deck) {
        return DeckSummaryDto.builder()
                .id(deck.getId())
                .name(deck.getName())
                .leaderCardId(deck.getLeaderCardId())
                .leaderColor(deck.getLeaderColor())
                .likesCount((long) deck.getLikesCount())
                .username(deck.getUser().getUsername())
                .avatarUrl(deck.getUser().getAvatarUrl())
                .createdAt(deck.getCreatedAt())
                .isPublic(deck.isPublic())
                .build();
    }

    private DeckResponse mapToResponseDto(Deck deck, Map<String, Integer> cards) {
        return DeckResponse.builder()
                .id(deck.getId())
                .name(deck.getName())
                .description(deck.getDescription())
                .leaderCardId(deck.getLeaderCardId())
                .isPublic(deck.isPublic())
                .createdAt(deck.getCreatedAt())
                .updatedAt(deck.getUpdatedAt())
                .likesCount((long) deck.getLikesCount())
                .userId(deck.getUser().getId())
                .username(deck.getUser().getUsername())
                .avatarUrl(deck.getUser().getAvatarUrl())
                .cards(cards)
                .build();
    }

    private void validateDeckRules(DeckRequest request) {
        if (request.getCards() == null || request.getCards().isEmpty()) {
            throw new BadRequestException("El mazo debe contener al menos una carta.");
        }

        // 1. Validar el Líder
        ExternalCardDto leader = cardService.getCardById(request.getLeaderCardId());
        if (leader == null) {
            throw new BadRequestException("El líder seleccionado (" + request.getLeaderCardId() + ") no existe.");
        }
        if (!"Leader".equalsIgnoreCase(leader.getType())) {
            throw new BadRequestException("La carta seleccionada como líder (" + leader.getName() + ") no es de tipo Leader.");
        }

        Set<String> leaderColors = parseColors(leader.getColor());

        int totalCards = 0;
        for (Map.Entry<String, Integer> entry : request.getCards().entrySet()) {
            String cardId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity == null || quantity < 1) {
                throw new BadRequestException("La cantidad de la carta " + cardId + " debe ser al menos 1.");
            }
            if (quantity > 4) {
                throw new BadRequestException("No puedes tener más de 4 copias de la carta " + cardId + ".");
            }
            totalCards += quantity;

            // 2. Validar existencia y color de la carta
            ExternalCardDto card = cardService.getCardById(cardId);
            if (card == null) {
                throw new BadRequestException("La carta " + cardId + " no existe.");
            }
            if ("DON!! Card".equalsIgnoreCase(card.getType())) {
                throw new BadRequestException("No puedes incluir cartas DON!! en el mazo principal.");
            }

            Set<String> cardColors = parseColors(card.getColor());
            for (String c : cardColors) {
                if (!leaderColors.contains(c)) {
                    throw new BadRequestException("La carta " + card.getName() + " tiene un color (" + c + 
                        ") que no está permitido por el líder " + leader.getName() + " (colores: " + leader.getColor() + ").");
                }
            }
        }

        if (totalCards > 50) {
            throw new BadRequestException("El mazo principal no puede exceder las 50 cartas (actualmente tienes " + totalCards + ").");
        }
    }

    private Set<String> parseColors(String colorString) {
        if (colorString == null || colorString.isBlank()) {
            return new HashSet<>();
        }
        return Arrays.stream(colorString.split("[\\s/,]+"))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toSet());
    }
}
