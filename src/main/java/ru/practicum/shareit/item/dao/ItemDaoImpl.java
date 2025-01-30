package ru.practicum.shareit.item.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ItemDaoImpl implements ItemDao {

    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item getItem(Long itemId) {
        log.info("Возвращаем вещь с id {}", itemId);
        if (itemId == null || itemId < 0) {
            throw new ValidationException("Id не может быть пустым или быть отрицательным числом");
        }
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь с ID " + itemId + " не найден!");
        }
        return item;
    }

    @Override
    public Collection<Item> getAllItems() {
        log.info("Возвращаем все вещи");
        return items.values();
    }

    @Override
    public Item createItem(Item item, Long ownerId) {
        log.info("Создаём вещь с id пользователя {}", ownerId);
        item.setId(getNextId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public void deleteItem(Long itemId) {
        log.info("Удалем вещь с id {}", itemId);
        if (itemId == null || itemId < 0) {
            throw new ValidationException("Id не может быть пустым или быть отрицательным числом");
        }
        items.remove(itemId);
    }

    @Override
    public Item updateItem(Map<String, String> update, Long itemId, Long ownerId) {
        log.info("Обновляем вещь с id {}", itemId);
        Item oldItem = items.get(itemId);
        if (items.get(itemId) == null) {
            throw new NotFoundException("Вещь с таким id не найден, обновление невозможно.");
        }
        if (!oldItem.getOwnerId().equals(ownerId)) {
            throw new ValidationException("Описание вещи может менять только владелец веши!");
        }
        if (update.get("name") != null) {
            if (!update.get("name").isBlank()) {
                oldItem.setName(update.get("name"));
            } else {
                throw new ValidationException("В запросе на обновление названия вещи была передана пустая строчка.");
            }
        }
        if (update.get("description") != null) {
            if (update.get("description").isBlank()) {
                throw new ValidationException("В запросе на обновление описания вещи была передана пустая строчка.");
            }
            oldItem.setDescription(update.get("description"));
        }
        if (update.get("available") != null) {
            if (!update.get("available").isBlank()) {
                oldItem.setAvailable(Boolean.parseBoolean(update.get("available")));
            } else {
                throw new ValidationException("В запросе на обновление статуса доступности " +
                        "была передана пустая строчка");
            }
        }
        items.put(oldItem.getId(), oldItem);
        return oldItem;
    }

    private long getNextId() {
        long currentMaxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
