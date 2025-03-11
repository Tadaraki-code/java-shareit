package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestDao;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItem;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestDao itemRequestDao;
    private final ItemDao itemDao;
    private final UserDao userDao;

    @Override
    public ItemRequestDto createItemRequest(ItemRequestDto request, Long ownerId) {
        User user = findUserById(ownerId);
        log.info("Передаём запрос на создание нового запроса с id пользователя {} в itemRequestDao.", ownerId);
        return ItemRequestMapper.toItemRequestDto(itemRequestDao
                .save(ItemRequestMapper.fromItemRequestDto(request, user)));
    }

    @Override
    public ItemRequestDtoWithItem getItemRequest(Long requestId) {
        ItemRequest itemRequest = findRequestById(requestId);
        Collection<Item> items = itemDao.findAllItemsByItemRequestId(requestId);
        log.info("Передаём запрос на получение запроса с id {}, в itemRequestDao.", requestId);
        return ItemRequestMapper.toItemRequestWithItem(itemRequest, items);
    }

    @Override
    public Collection<ItemRequestDtoWithItem> getAllOwnerRequest(Long ownerId) {
        findUserById(ownerId);
        Collection<ItemRequest> itemRequests = itemRequestDao.findAllOwnerRequest(ownerId);
        Collection<Item> items = itemDao.findAllItemsByItemsRequestIds(itemRequests.stream()
                .map(ItemRequest::getId)
                .toList());

        Map<Long, List<Item>> itemsMap = items.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        log.info("Передаём запрос от владельца с id {}, на получение списка своих запросов в itemRequestDao.", ownerId);
        return itemRequests.stream()
                .map(ir -> ItemRequestMapper.toItemRequestWithItem(ir, itemsMap.get(ir.getId())))
                .toList();
    }

    @Override
    public Collection<ItemRequestDto> getAllRequest(Long userId) {
        findUserById(userId);
        Collection<ItemRequest> allRequest = itemRequestDao.findAllByNotRequesterId(userId);
        log.info("Передаём запрос от пользователя с id {}, на получение списка всех запросов в ownerId.", userId);
        return allRequest.stream().map(ItemRequestMapper::toItemRequestDto).toList();
    }

    @Override
    public void deleteItemRequest(Long ownerId, Long requestId) {
        findUserById(ownerId);
        findRequestById(requestId);
        itemRequestDao.deleteById(requestId);
    }

    private User findUserById(Long userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь c id " + userId + " не найден."));
    }

    private ItemRequest findRequestById(Long requestId) {
        return itemRequestDao.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос c id " + requestId + " не найден."));
    }

}
