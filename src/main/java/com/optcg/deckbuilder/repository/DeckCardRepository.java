package com.optcg.deckbuilder.repository;

import com.optcg.deckbuilder.model.entity.DeckCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckCardRepository extends JpaRepository<DeckCard, Long> {
    List<DeckCard> findByDeckId(Long deckId);
    void deleteByDeckId(Long deckId);
}
