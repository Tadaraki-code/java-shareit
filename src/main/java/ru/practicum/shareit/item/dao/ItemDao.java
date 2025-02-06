package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemDao extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))) AND i.available = TRUE")
    Collection<Item> findByNameContainingOrDescriptionContaining(@Param("text") String text);

    @Query("SELECT i FROM Item i WHERE i.owner.id = :id")
    Collection<Item> findByOwnerId(Long id);

}
