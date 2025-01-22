package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final UserDao userDao;

    @Override
    public ItemDto getItem(Long id) {
        log.info("Передаём запрос на получение вещи в itemDao.");
        return ItemMapper.toItemDto(itemDao.getItem(id));
    }

    @Override
    public List<ItemDto> getAllOwnerItems(Long ownerId) {
        log.info("Передаём запрос на список всех вещей пользоваля с id{} из itemDao.", ownerId);
        userDao.getUser(ownerId);
        return itemDao.getAllItems()
                .stream()
                .filter(item -> Objects.equals(item.getOwnerId(), ownerId))
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Передаём запрос на создание новой вещи с id пользователя {} в itemDao.", ownerId);
        userDao.getUser(ownerId);
        return ItemMapper.toItemDto(itemDao.createItem(ItemMapper.fromItemDto(itemDto, ownerId), ownerId));
    }

    @Override
    public void deleteItem(Long id) {
        log.info("Передаём запрос на удаление вещи с id {} в itemDao.", id);
        itemDao.deleteItem(id);
    }

    @Override
    public ItemDto updateItem(Map<String, String> update, Long itemId, Long ownerId) {
        log.info("Передаём запрос на обновление вещи с id {} в itemDao.", itemId);
        userDao.getUser(ownerId);
        return ItemMapper.toItemDto(itemDao.updateItem(update, itemId, ownerId));
    }

    @Override
    public List<ItemDto> searchItems(String searchText) {
        log.info("Передаём запрос на поиск вещи с текстом {} в itemDao.", searchText);
        if (searchText == null || searchText.isBlank()) {
            return List.of();
        }
        return itemDao.getAllItems()
                .stream()
                .filter(Item::isAvailable)
                .filter(item -> item.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(searchText.toLowerCase()))
                .map(ItemMapper::toItemDto)
                .toList();
    }
}
