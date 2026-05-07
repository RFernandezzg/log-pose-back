package com.optcg.deckbuilder.repository;

import com.optcg.deckbuilder.model.entity.ShopItem;
import com.optcg.deckbuilder.model.enums.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {
    List<ShopItem> findByCategory(ItemCategory category);
}
