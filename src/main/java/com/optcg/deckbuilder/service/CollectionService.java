package com.optcg.deckbuilder.service;

import com.optcg.deckbuilder.model.dto.collection.CollectionItemDto;
import com.optcg.deckbuilder.model.entity.User;
import com.optcg.deckbuilder.model.entity.UserCollection;
import com.optcg.deckbuilder.exception.BadRequestException;
import com.optcg.deckbuilder.exception.NotFoundException;
import com.optcg.deckbuilder.repository.UserCollectionRepository;
import com.optcg.deckbuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final UserCollectionRepository collectionRepository;
    private final UserRepository userRepository;

    public List<CollectionItemDto> getMyCollection(String username) {
        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new NotFoundException("User not found"));

        return collectionRepository.findByUser(user).stream()
                .map(item -> CollectionItemDto.builder()
                        .id(item.getId())
                        .cardId(item.getCardId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public CollectionItemDto addCardToCollection(String cardId, Integer quantity, String username) {
                if (quantity == null || quantity <= 0) {
                        throw new BadRequestException("Quantity must be greater than zero");
                }

        User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new NotFoundException("User not found"));

        UserCollection collectionItem = collectionRepository.findByUserAndCardId(user, cardId)
                .orElse(UserCollection.builder()
                        .user(user)
                        .cardId(cardId)
                        .quantity(0)
                        .build());

        collectionItem.setQuantity(collectionItem.getQuantity() + quantity);
        UserCollection savedItem = collectionRepository.save(collectionItem);

        return CollectionItemDto.builder()
                .id(savedItem.getId())
                .cardId(savedItem.getCardId())
                .quantity(savedItem.getQuantity())
                .build();
    }

    @Transactional
    public CollectionItemDto updateCardQuantity(String cardId, Integer quantity, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserCollection collectionItem = collectionRepository.findByUserAndCardId(user, cardId)
                .orElseThrow(() -> new NotFoundException("Card not found in collection"));

        if (quantity <= 0) {
            collectionRepository.delete(collectionItem);
            return null; // Successfully removed
        }

        collectionItem.setQuantity(quantity);
        UserCollection savedItem = collectionRepository.save(collectionItem);

        return CollectionItemDto.builder()
                .id(savedItem.getId())
                .cardId(savedItem.getCardId())
                .quantity(savedItem.getQuantity())
                .build();
    }

    @Transactional
    public void removeCardFromCollection(String cardId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserCollection collectionItem = collectionRepository.findByUserAndCardId(user, cardId)
                .orElseThrow(() -> new NotFoundException("Card not found in collection"));

        collectionRepository.delete(collectionItem);
    }
}
