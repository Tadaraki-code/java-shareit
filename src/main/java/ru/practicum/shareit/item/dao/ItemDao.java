package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Map;

public interface ItemDao {

    Item getItem(Long id);

    Collection<Item> getAllItems();

    Item createItem(Item item, Long id);

    void deleteItem(Long id);

    Item updateItem(Map<String, String> update, Long itemId, Long ownerId);
}
