package com.optcg.deckbuilder.repository;

import com.optcg.deckbuilder.model.entity.UserCollection;
import com.optcg.deckbuilder.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCollectionRepository extends JpaRepository<UserCollection, Long> {
    List<UserCollection> findByUser(User user);
    Optional<UserCollection> findByUserAndCardId(User user, String cardId);
}
