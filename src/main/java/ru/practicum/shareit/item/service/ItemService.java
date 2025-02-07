package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWhitBooking;
import ru.practicum.shareit.item.dto.ItemDtoWhitComments;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ItemService {

    ItemDtoWhitComments getItem(Long id);

    Collection<ItemDtoWhitBooking> getAllOwnerItems(Long ownerId);

    ItemDto createItem(ItemDto item, Long ownerI);

    void deleteItem(Long id, Long ownerId);

    ItemDto updateItem(Map<String, String> update, Long itemId, Long ownerId);

    List<ItemDto> searchItems(String searchText);

    CommentDto createComment(CommentDto comment, Long itemId, Long ownerId);

}
