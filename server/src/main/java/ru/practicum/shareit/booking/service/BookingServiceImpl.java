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
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new ValidationException("Дата начала бронирования не может быть позже конца бронирования");
        }

        Item item = itemDao.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь c id " + bookingDto.getItemId() + " не найдена."));
        if (!item.isAvailable()) {
            throw new ValidationException("Бронирование невозможно, вещь с id " + item.getId() +
                    " недоступна для бронирования.");
        }

        User user = findUserById(userId);

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(Status.WAITING);

        log.info("Передаём запрос на создание бронирования в bookingDao.");
        return BookingMapper.toBookingDto(bookingDao.save(booking));
    }

    @Override
    public BookingDto updateBookingStatus(Long bookingId, boolean approveState, Long userId) {
        Booking oldBooking = findBookingById(bookingId);
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
        findUserById(userId);
        Booking booking = findBookingById(bookingId);
        if (booking.getItem().getOwner().getId().equals(userId) || booking.getBooker().getId().equals(userId)) {
            return BookingMapper.toBookingDto(booking);
        }
        throw new ValidationException("Только автор бронирования или владелец вещи " +
                "могут получить данные о бронировании");
    }

    @Override
    public Collection<BookingDto> getUserBookings(String state, Long userId) {
        findUserById(userId);
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
        return requiredBookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public Collection<BookingDto> getOwnerBookings(String state, Long userId) {
        findUserById(userId);
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
        return requiredBookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public void deleteBooking(Long bookingId, Long userId) {
        log.info("Передаём запрос на удаление бронирования с id{} в bookingDao.", bookingId);
        Booking booking = findBookingById(bookingId);
        findUserById(userId);
        if (!booking.getBooker().getId().equals(userId)) {
            throw new ValidationException("Только создателя бронирования может его удалить");
        }
        bookingDao.deleteById(bookingId);
    }

    private User findUserById(Long userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь c id " + userId + " не найден."));
    }

    private Booking findBookingById(Long bookingId) {
        return bookingDao.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь c id " + bookingId + " не найдена."));
    }
}
