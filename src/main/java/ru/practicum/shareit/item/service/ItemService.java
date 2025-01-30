package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ItemService {

    ItemDto getItem(Long id);

    Collection<ItemDto> getAllOwnerItems(Long ownerId);

    ItemDto createItem(ItemDto item, Long ownerI);

    void deleteItem(Long id);

    ItemDto updateItem(Map<String, String> update, Long itemId, Long ownerId);

    List<ItemDto> searchItems(String searchText);

}
