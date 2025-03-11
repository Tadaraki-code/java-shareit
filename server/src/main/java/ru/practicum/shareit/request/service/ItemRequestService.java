package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItem;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto createItemRequest(ItemRequestDto request, Long ownerId);

    ItemRequestDtoWithItem getItemRequest(Long itemRequestId);

    Collection<ItemRequestDtoWithItem> getAllOwnerRequest(Long ownerId);

    Collection<ItemRequestDto> getAllRequest(Long ownerId);

    void deleteItemRequest(Long ownerId, Long requestId);
}
