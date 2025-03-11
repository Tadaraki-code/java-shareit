package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingDao;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.CommentDao;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWhitBooking;
import ru.practicum.shareit.item.dto.ItemDtoWhitComments;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestDao;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final UserDao userDao;
    private final BookingDao bookingDao;
    private final CommentDao commentDao;
    private final ItemRequestDao itemRequestDao;

    @Override
    public ItemDtoWhitComments getItem(Long id) {
        log.info("Передаём запрос на получение вещи с id {} в itemDao.", id);
        Item item = findItemById(id);
        return ItemMapper.toItemDtoWhitComments(item, commentDao.findAllCommentsByItemId(item.getId()));
    }

    @Override
    public List<ItemDtoWhitBooking> getAllOwnerItems(Long ownerId) {
        log.info("Передаём запрос на список всех вещей пользоваля с id{} из itemDao.", ownerId);
        findUserById(ownerId);
        Collection<Item> userItems = itemDao.findByOwnerId(ownerId);
        List<Long> itemIds = userItems.stream().map(Item::getId).toList();
        LocalDateTime now = LocalDateTime.now();

        if (itemIds.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<Booking> itemsBooking = bookingDao.findAllRelevantApprovedOwnerItemsBookingsById(itemIds, now);

        Collection<Comment> itemsComments = commentDao.findAllCommentsForAllItemsById(itemIds);

        Map<Long, List<Booking>> bookingMap = itemsBooking.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Long, List<Comment>> commentMap = itemsComments.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));


        return userItems.stream()
                .map(item -> {
                    List<Booking> bookings = bookingMap.getOrDefault(item.getId(), Collections.emptyList());
                    Collection<Comment> comment = commentMap.getOrDefault(item.getId(), Collections.emptyList());
                    Booking lastBooking = bookings.stream()
                            .filter(booking -> booking.getEnd().isBefore(now))
                            .max(Comparator.comparing(Booking::getEnd))
                            .orElse(null);
                    Booking nextBooking = bookings.stream()
                            .filter(booking -> booking.getStart().isAfter(now))
                            .min(Comparator.comparing(Booking::getStart))
                            .orElse(null);
                    return ItemMapper.toItemDtoWhitBooking(item, lastBooking, nextBooking, comment);
                })
                .toList();
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Передаём запрос на создание новой вещи с id пользователя {} в itemDao.", ownerId);
        User user = findUserById(ownerId);
        ItemRequest request = findRequestById(itemDto);
        return ItemMapper.toItemDto(itemDao.save(ItemMapper.fromItemDto(itemDto, user, request)));
    }

    @Override
    public void deleteItem(Long itemId, Long ownerId) {
        log.info("Передаём запрос на удаление вещи с id {}  от пользлвателя с id {} в itemDao.", itemId, ownerId);
        findUserById(ownerId);
        Item item = findItemById(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ValidationException("Только владелец вещи может удалить вещь");
        }
        itemDao.deleteById(itemId);
    }

    @Override
    public ItemDto updateItem(Map<String, String> update, Long itemId, Long ownerId) {
        log.info("Передаём запрос на обновление вещи с id {} в itemDao.", itemId);
        findUserById(ownerId);
        Item oldItem = findItemById(itemId);

        if (!oldItem.getOwner().getId().equals(ownerId)) {
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
        itemDao.save(oldItem);
        return ItemMapper.toItemDto(oldItem);
    }

    @Override
    public List<ItemDto> searchItems(String searchText) {
        log.info("Передаём запрос на поиск вещи с текстом {} в itemDao.", searchText);
        if (searchText == null || searchText.isBlank()) {
            return List.of();
        }
        return itemDao.findByNameContainingOrDescriptionContaining(searchText).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto createComment(CommentDto comment, Long itemId, Long ownerId) {
        Item item = findItemById(itemId);
        User user = findUserById(ownerId);
        Booking booking = bookingDao.findBookingByUserIdAndItemId(ownerId, itemId)
                .orElseThrow(() -> new NotFoundException("Бронь не найден"));
        if (booking.getStatus().equals(Status.APPROVED) && booking.getEnd().isBefore(LocalDateTime.now())) {
            log.info("Передаём запрос на создание отзыва в commentDao.");
            return CommentMapper.toCommentDto(commentDao.save(CommentMapper.fromCommentDto(comment, item, user)));
        }
        throw new ValidationException("Вы не можете оставить отзыв о вещи с id " +
                itemId + ", поскольку вы не брали её в аренду или срок аренды ещё не истёк.");
    }

    private Item findItemById(Long itemId) {
        return itemDao.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

    private User findUserById(Long userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь c id " + userId + " не найден."));
    }

    private ItemRequest findRequestById(ItemDto item) {
        if (item.getRequestId() == null) {
            return null;
        }
        return itemRequestDao.findById(item.getRequestId())
                .orElseThrow(() -> new NotFoundException("Запрос с id " + item.getRequestId() + " не найден."));
    }
}

