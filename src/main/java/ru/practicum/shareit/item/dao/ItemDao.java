package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Map;

public interface ItemDao {

    ItemDto getItem(Long id);

    Collection<Item> getAllItems();

    ItemDto createItem(ItemDto item, Long id);

    void deleteItem(Long id);

    ItemDto updateItem(Map<String, String> update, Long itemId, Long ownerId);
}
