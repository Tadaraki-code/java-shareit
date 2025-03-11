package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItem;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated());
    }

    public static ItemRequest fromItemRequestDto(ItemRequestDto itemRequestDto, User requestor) {
        return new ItemRequest(itemRequestDto.getId(),
                itemRequestDto.getDescription(),
                requestor,
                LocalDateTime.now());
    }

    public static ItemRequestDtoWithItem toItemRequestWithItem(ItemRequest itemRequest, Collection<Item> items) {
        List<ItemDtoForRequest> itemsForRequests = (items == null || items.isEmpty())
                ? Collections.emptyList()
                : items.stream().map(ItemMapper::toItemDtoForRequest).toList();

        return new ItemRequestDtoWithItem(itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                itemsForRequests);
    }
}
