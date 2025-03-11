package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItem;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createRequest(@RequestBody ItemRequestDto requestDto,
                                        @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос из gateway на создание нового запроса.");
        return itemRequestService.createItemRequest(requestDto, ownerId);
    }

    @GetMapping
    public Collection<ItemRequestDtoWithItem> getOwnerRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос из gateway от владельца с id {}, на получение списка своих запросов.", userId);
        return itemRequestService.getAllOwnerRequest(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAllOtherRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос из gateway от пользователя с id {}, на получение списка всех запросов.", userId);
        return itemRequestService.getAllRequest(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoWithItem getRequestById(@PathVariable Long requestId) {
        log.info("Запрос из gateway на получение запроса с id {}.", requestId);
        return itemRequestService.getItemRequest(requestId);
    }

    @DeleteMapping("/{id}")
    public void deleteRequestById(@PathVariable("id") Long requestId, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос из gateway на удаление запроса с id {}, от пользователя с id {}.", requestId, ownerId);
        itemRequestService.deleteItemRequest(ownerId, requestId);
    }
}
