package com.optcg.deckbuilder.repository;

import com.optcg.deckbuilder.model.entity.DeckLike;
import com.optcg.deckbuilder.model.entity.Deck;
import com.optcg.deckbuilder.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeckLikeRepository extends JpaRepository<DeckLike, Long> {
    boolean existsByDeckAndUser(Deck deck, User user);
    Optional<DeckLike> findByDeckAndUser(Deck deck, User user);
    long countByDeck(Deck deck);
    void deleteByDeck(Deck deck);
}
