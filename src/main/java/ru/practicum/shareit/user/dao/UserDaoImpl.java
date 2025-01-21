package ru.practicum.shareit.user.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {

    private final Map<Long, User> users = new HashMap<>();
    private final ItemDao itemDao;

    @Override
    public User getUser(Long id) {
        log.info("Возвращаем пользователя с id {}", id);
        if (id == null || id < 0) {
            throw new ValidationException("Id не может быть пустым или быть отрицательным числом");
        }
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользоватль с ID " + id + " не найден!");
        }
        return user;
    }

    @Override
    public Collection<User> getAllUser() {
        log.info("Возвращаем всех пользователя");
        return users.values();
    }

    @Override
    public User createUser(User user) {
        log.info("Добавляем пользователя {}", user);
        if (!checkEmails(user.getEmail())) {
            throw new AlreadyExistException("Пользователь с email " + user.getEmail() + " уже существует");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(Long id) {
        if (id == null || id < 0) {
            throw new ValidationException("Id не может быть пустым или быть отрицательным числом");
        }
        Collection<Item> items = itemDao.getAllItems();
        for (Item i : items) {
            if (i.getOwnerId().equals(id)) {
                itemDao.deleteItem(i.getId());
            }
        }
        log.info("Удаляем пользователя c id {}", id);
        users.remove(id);
    }

    @Override
    public User updateUser(Map<String, String> update, Long id) {
        log.info("Обновляем пользователя с id {}", id);
        if (users.get(id) == null) {
            throw new NotFoundException("Пользоватеь с таким id не найден, обновление невозможно.");
        }
        User oldUser = users.get(id);
        users.remove(id);
        if (update.get("name") != null) {
            if (!update.get("name").isBlank()) {
                oldUser.setName(update.get("name"));
            } else {
                users.put(oldUser.getId(), oldUser);
                throw new ValidationException("В запросе на обновление имени была передана пустая строчка");
            }
        }
        if (update.get("email") != null) {
            if (update.get("email").isBlank()) {
                users.put(oldUser.getId(), oldUser);
                throw new ValidationException("В запросе на обновление имейла была передана пустая строчка");
            }
            if (checkEmails(update.get("email"))) {
                oldUser.setEmail(update.get("email"));
            } else {
                users.put(oldUser.getId(), oldUser);
                throw new AlreadyExistException("Пользователь с email " + update.get("email") + " уже существует");
            }
        }
        users.put(oldUser.getId(), oldUser);
        return oldUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private Boolean checkEmails(String email) {
        for (User u : users.values()) {
            if (u.getEmail().equals(email)) {
                return false;
            }
        }
        return true;
    }
}
