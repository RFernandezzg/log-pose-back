package com.optcg.deckbuilder.repository;

import com.optcg.deckbuilder.model.entity.Deck;
import com.optcg.deckbuilder.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {
    List<Deck> findByUser(User user);
    @org.springframework.data.jpa.repository.Query("SELECT d FROM Deck d WHERE d.isPublic = true " +
            "AND (:leaderCardId IS NULL OR d.leaderCardId = :leaderCardId) " +
            "AND (:leaderColor IS NULL OR LOWER(d.leaderColor) LIKE LOWER(CONCAT('%', :leaderColor, '%')))")
    Page<Deck> findByIsPublicTrueWithFilters(
            @org.springframework.data.repository.query.Param("leaderCardId") String leaderCardId,
            @org.springframework.data.repository.query.Param("leaderColor") String leaderColor,
            Pageable pageable);
}
