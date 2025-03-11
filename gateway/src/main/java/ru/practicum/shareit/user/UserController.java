package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.util.Map;


@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("Запрос на получение списка всех пользователя");
        return userClient.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findUser(@PathVariable("id") Long id) {
        log.info("Запрос на получение пользователя с id {}", id);
        return userClient.findUser(id);
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserRequestDto userDto) {
        log.info("Запрос на добавлен пользователя {}", userDto);
        return userClient.create(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestBody Map<String, String> update, @PathVariable("id") Long id) {
        log.info("Запрос на обновление пользователя с id {}", id);
        return userClient.update(update, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable("id") Long id) {
        log.info("Запрос на удаление пользователя c id {}", id);
        return userClient.deleteUser(id);
    }
}
