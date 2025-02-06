package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingDao;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.RequestBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingDao bookingDao;
    private final UserDao userDao;
    private final ItemDao itemDao;

    @Override
    public BookingDto createBooking(RequestBookingDto bookingDto, Long userId) {
        Item item = itemDao.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найден."));
        if (item.isAvailable()) {
            User user = userDao.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
            log.info("Передаём запрос на создание бронирования в bookingDao.");
            return BookingMapper.toBookingDto(bookingDao.save(BookingMapper.fromBookingDto(bookingDto, user, item)));
        }
        throw new ValidationException("Бронирование невозможно, вещь с id " + item.getId() +
                " недоступна для бронирования.");
    }

    @Override
    public BookingDto updateBookingStatus(Long bookingId, boolean approveState, Long userId) {
        Booking oldBooking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найден."));
        if (oldBooking.getItem().getOwner().getId().equals(userId)) {
            if (approveState) {
                oldBooking.setStatus(Status.APPROVED);
            } else {
                oldBooking.setStatus(Status.REJECTED);
            }
        } else {
            throw new ValidationException("Только владелец вещи может менять статус бронирования!");
        }
        log.info("Передаём запрос на обновление статуса бронирования с id{} в bookingDao.", bookingId);
        bookingDao.save(oldBooking);
        return BookingMapper.toBookingDto(oldBooking);
    }

    @Override
    public BookingDto getBooking(Long bookingId, Long userId) {
        log.info("Передаём запрос на получение информации о бронирование с id{} в bookingDao.", bookingId);
        Booking booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найден."));
        if (booking.getItem().getOwner().getId().equals(userId) || booking.getBooker().getId().equals(userId)) {
            return BookingMapper.toBookingDto(booking);
        }
        throw new ValidationException("Только автор бронирования или владелец вещи " +
                "могут получить данные о бронировании");
    }

    @Override
    public Collection<BookingDto> getUserBookings(String state, Long userId) {
        User user = userDao.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        log.info("Передаём запрос на получение информации о бронирование пользователя с id{} в bookingDao.", userId);
        Collection<Booking> requiredBookings = switch (state) {
            case "ALL" -> bookingDao.findAllUserBookingByUserId(userId);
            case "CURRENT" -> bookingDao.findCurrentUserBookingByUserId(userId, LocalDateTime.now());
            case "PAST" -> bookingDao.findPastUserBookingByUserId(userId, LocalDateTime.now());
            case "FUTURE" -> bookingDao.findFutureUserBookingByUserId(userId, LocalDateTime.now());
            case "WAITING" -> bookingDao.findWaitingUserBookingByUserId(userId);
            case "REJECTED" -> bookingDao.findRejectedUserBookingByUserId(userId);
            default -> throw new ValidationException("Неверный параметр состояния: " + state);
        };
        return requiredBookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    @Override
    public Collection<BookingDto> getOwnerBookings(String state, Long userId) {
        User user = userDao.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        Collection<Long> userItems = itemDao.findByOwnerId(userId).stream()
                .map(Item::getId)
                .toList();
        Collection<Booking> requiredBookings = Collections.emptyList();
        log.info("Передаём запрос на получение информации о бронирование владельца вещи с id{} в bookingDao.", userId);
        if (!userItems.isEmpty()) {
            requiredBookings = switch (state) {
                case "ALL" -> bookingDao.findAllOwnerItemsBookingById(userItems);
                case "CURRENT" -> bookingDao.findCurrentOwnerItemsBookingById(userItems, LocalDateTime.now());
                case "PAST" -> bookingDao.findPastOwnerItemsBookingById(userItems, LocalDateTime.now());
                case "FUTURE" -> bookingDao.findFutureOwnerItemsBookingById(userItems, LocalDateTime.now());
                case "WAITING" -> bookingDao.findWaitingOwnerItemsBookingById(userItems);
                case "REJECTED" -> bookingDao.findRejectedOwnerItemsBookingById(userItems);
                default -> throw new ValidationException("Неверный параметр состояния: " + state);
            };
        }
        return requiredBookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    @Override
    public void deleteBooking(Long bookingId) {
        log.info("Передаём запрос на удаление бронирования с id{} в bookingDao.", bookingId);
        bookingDao.deleteById(bookingId);
    }
}
