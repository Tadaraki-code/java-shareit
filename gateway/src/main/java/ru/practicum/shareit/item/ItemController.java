package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import java.util.Map;


@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody ItemRequestDto itemDto,
                                             @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на создание новой вещи.");
        return itemClient.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestBody Map<String, String> update,
                                             @PathVariable("id") Long itemId,
                                             @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на обновление вещи с id {}", itemId);
        return itemClient.updateItem(update, itemId, ownerId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getItem(@PathVariable("id") Long id) {
        log.info("Запрос на получение вещи с id {}", id);
        return itemClient.getItem(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwnerItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на получение  всех вещий пользоватля с id {}", ownerId);
        return itemClient.getAllOwnerItems(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text,
                                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на поиск всех вещий с текстом {}", text);
        return itemClient.searchItems(text, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable("id") Long id, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на удалние вещи с id {}", id);
        itemClient.deleteItem(id, ownerId);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> createComment(@RequestBody CommentDto comment,
                                                @PathVariable("id") Long itemId,
                                                @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на создание нового отзыва.");
        return itemClient.createComment(comment, itemId, ownerId);
    }
}
