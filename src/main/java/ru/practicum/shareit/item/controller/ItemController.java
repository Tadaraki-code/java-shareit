package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWhitBooking;
import ru.practicum.shareit.item.dto.ItemDtoWhitComments;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@Valid @RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на создание новой вещи.");
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestBody Map<String, String> update,
                              @PathVariable("id") Long itemId,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на обновление вещи с id {}", itemId);
        return itemService.updateItem(update, itemId, ownerId);
    }

    @GetMapping("/{id}")
    public ItemDtoWhitComments getItem(@PathVariable("id") Long id) {
        log.info("Запрос на получение вещи с id {}", id);
        return itemService.getItem(id);
    }

    @GetMapping
    public Collection<ItemDtoWhitBooking> getAllOwnerItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на получение  всех вещий пользоватля с id {}", ownerId);
        return itemService.getAllOwnerItems(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("Запрос на поиск всех вещий с текстом {}", text);
        return itemService.searchItems(text);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable("id") Long id, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на удалние вещи с id {}", id);
        itemService.deleteItem(id, ownerId);
    }

    @PostMapping("/{id}/comment")
    public CommentDto createComment(@RequestBody CommentDto comment,
                                    @PathVariable("id") Long itemId,
                                    @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на создание нового отзыва.");
        return itemService.createComment(comment, itemId, ownerId);
    }
}
