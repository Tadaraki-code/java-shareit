package ru.practicum.shareit.request;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@Valid @RequestBody RequestDto requestDto,
                                                @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на создание нового запроса.");
        return requestClient.createRequest(requestDto, ownerId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос от владельца с id {}, на получение списка своих запросов.", userId);
        return requestClient.getOwnerRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOtherRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос от пользователя с id {}, на получение списка всех запросов.", userId);
        return requestClient.getAllOtherRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId,
                                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Запрос на получение запроса с id {}.", requestId);
        return requestClient.getRequestById(requestId, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteRequestById(@PathVariable("id") Long requestId, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Запрос на удаление запроса с id {}, от пользователя с id {}.", requestId, ownerId);
        requestClient.deleteRequestById(ownerId, requestId);
    }
}
