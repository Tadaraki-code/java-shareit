package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAll() {
        log.info("Запрос из gateway на получение списка всех пользователя");
        return userService.getAllUser();
    }

    @GetMapping("/{id}")
    public UserDto findUser(@PathVariable("id") Long id) {
        log.info("Запрос из gateway на получение пользователя с id {}", id);
        return userService.getUser(id);
    }

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        log.info("Запрос из gateway на добавлен пользователя {}", userDto);
        return userService.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@RequestBody Map<String, String> update, @PathVariable("id") Long id) {
        log.info("Запрос из gateway на обновление пользователя с id {}", id);
        return userService.updateUser(update, id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        log.info("Запрос из gateway на удаление пользователя c id {}", id);
        userService.deleteUser(id);
    }
}
