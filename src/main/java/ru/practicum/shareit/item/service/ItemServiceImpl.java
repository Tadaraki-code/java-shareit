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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemDao itemDao;
    private final UserDao userDao;
    private final BookingDao bookingDao;
    private final CommentDao commentDao;

    @Override
    public ItemDtoWhitComments getItem(Long id) {
        log.info("Передаём запрос на получение вещи в itemDao.");
        Item item = itemDao.findById(id).orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена."));
        return ItemMapper.toItemDtoWhitComments(item,commentDao.findAllCommentsByItemId(item.getId()));
    }

    @Override
    public List<ItemDtoWhitBooking> getAllOwnerItems(Long ownerId) {
        log.info("Передаём запрос на список всех вещей пользоваля с id{} из itemDao.", ownerId);
        userDao.findById(ownerId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        return itemDao.findByOwnerId(ownerId).stream()
                .map(i -> ItemMapper.toItemDtoWhitBooking(i,
                        bookingDao.findLastBooking(i.getId(), LocalDateTime.now()),
                        bookingDao.findNextBooking(i.getId(), LocalDateTime.now()),
                        commentDao.findAllCommentsByItemId(i.getId())))
                .toList();
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Передаём запрос на создание новой вещи с id пользователя {} в itemDao.", ownerId);
        User user = userDao.findById(ownerId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        return ItemMapper.toItemDto(itemDao.save(ItemMapper.fromItemDto(itemDto, user)));
    }

    @Override
    public void deleteItem(Long id) {
        log.info("Передаём запрос на удаление вещи с id {} в itemDao.", id);
        itemDao.deleteById(id);
    }

    @Override
    public ItemDto updateItem(Map<String, String> update, Long itemId, Long ownerId) {
        log.info("Передаём запрос на обновление вещи с id {} в itemDao.", itemId);
        userDao.findById(ownerId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        Item oldItem = itemDao.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена."));

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
        System.out.println(itemDao.findByNameContainingOrDescriptionContaining(searchText));
        return itemDao.findByNameContainingOrDescriptionContaining(searchText).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto createComment(CommentDto comment, Long itemId, Long ownerId) {
        Item item = itemDao.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещи с id " + itemId + " не найдена"));
        User user = userDao.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + ownerId + " не найден"));
        Booking booking = bookingDao.findBookingByUserIdAndItemId(ownerId,itemId)
                .orElseThrow(() -> new NotFoundException("Бронь не найден"));
        if (booking.getStatus().equals(Status.APPROVED) && booking.getEnd().isBefore(LocalDateTime.now())) {
            log.info("Передаём запрос на создание отзыва в commentDao.");
            return CommentMapper.toCommentDto(commentDao.save(CommentMapper.fromCommentDto(comment,item,user)));
        }
        throw new ValidationException("Вы не можете оставить отзыв о вещи с id " +
                itemId + ", поскольку вы не брали её в аренду или срок аренды ещё не истёк.");
    }
}

