package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.model.User;
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
    public Collection<User> findAll() {
        log.info("Запрос на получение списка всех пользователя");
        return userService.getAllUser();
    }

    @GetMapping("/{id}")
    public User findUser(@PathVariable("id") Long id) {
        log.info("Запрос на получение пользователя с id {}", id);
        return userService.getUser(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Запрос на добавлен пользователя {}", user);
        userService.createUser(user);
        return user;
    }

    @PatchMapping("/{id}")
    public User update(@RequestBody Map<String, String> update, @PathVariable("id") Long id) {
        log.info("Запрос на обновление пользователя с id {}", id);
        return userService.updateUser(update, id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        log.info("Запрос на удаление пользователя c id {}", id);
        userService.deleteUser(id);
    }
}
