package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dao.BookingDao;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.RequestBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingDao bookingDao;

    @Mock
    private UserDao userDao;

    @Mock
    private ItemDao itemDao;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private Item item;
    private Booking booking;
    private RequestBookingDto requestBookingDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User1", "user1@example.com");
        item = new Item(1L, "Item1", "Description", true, user, null);
        requestBookingDto = new RequestBookingDto(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        booking = BookingMapper.fromBookingDto(requestBookingDto, user, item);
        booking.setId(1L);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);
    }

    @Test
    void createBookingShouldReturnBookingDtoWhenValid() {
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(bookingDao.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.createBooking(requestBookingDto, 1L);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getItem().getId(), result.getItem().getId());
        assertEquals(booking.getBooker().getId(), result.getBooker().getId());
        assertEquals(Status.WAITING, result.getStatus());
        verify(bookingDao, times(1)).save(any(Booking.class));
    }

    @Test
    void createBookingShouldThrowValidationExceptionWhenStartAfterEnd() {
        RequestBookingDto invalidDto = new RequestBookingDto(1L,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(invalidDto, 1L));
        assertEquals("Дата начала бронирования не может быть позже конца бронирования",
                exception.getMessage());
        verify(bookingDao, never()).save(any());
    }

    @Test
    void createBookingShouldThrowNotFoundExceptionWhenItemNotFound() {
        when(itemDao.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(requestBookingDto, 1L));
        assertEquals("Вещь c id 1 не найдена.", exception.getMessage());
        verify(bookingDao, never()).save(any());
    }

    @Test
    void createBookingShouldThrowValidationExceptionWhenItemNotAvailable() {
        item.setAvailable(false);
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(requestBookingDto, 1L));
        assertEquals("Бронирование невозможно, вещь с id 1 недоступна для бронирования.",
                exception.getMessage());
        verify(bookingDao, never()).save(any());
    }

    @Test
    void createBookingShouldThrowNotFoundExceptionWhenUserNotFound() {
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(requestBookingDto, 1L));
        assertEquals("Пользователь c id 1 не найден.", exception.getMessage());
        verify(bookingDao, never()).save(any());
    }

    @Test
    void updateBookingStatusShouldApproveBookingWhenOwner() {
        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingDao.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            savedBooking.setStatus(Status.APPROVED);
            return savedBooking;
        });

        BookingDto result = bookingService.updateBookingStatus(1L, true, 1L);

        assertNotNull(result);
        assertEquals(Status.APPROVED, result.getStatus());
        verify(bookingDao, times(1)).save(any(Booking.class));
    }

    @Test
    void updateBookingStatusShouldRejectBookingWhenOwner() {
        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingDao.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            savedBooking.setStatus(Status.REJECTED);
            return savedBooking;
        });

        BookingDto result = bookingService.updateBookingStatus(1L, false, 1L);

        assertNotNull(result);
        assertEquals(Status.REJECTED, result.getStatus());
        verify(bookingDao, times(1)).save(any(Booking.class));
    }

    @Test
    void updateBookingStatusShouldThrowValidationExceptionWhenNotOwner() {
        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.updateBookingStatus(1L, true, 2L));
        assertEquals("Только владелец вещи может менять статус бронирования!", exception.getMessage());
        verify(bookingDao, never()).save(any());
    }

    @Test
    void getBookingShouldReturnBookingDtoWhenOwner() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getBooking(1L, 1L);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getItem().getId(), result.getItem().getId());
        assertEquals(booking.getBooker().getId(), result.getBooker().getId());
    }

    @Test
    void getBookingShouldThrowValidationExceptionWhenNotOwnerOrBooker() {
        User otherUser = new User(2L, "User2", "user2@example.com");
        when(userDao.findById(2L)).thenReturn(Optional.of(otherUser));
        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getBooking(1L, 2L));
        assertEquals("Только автор бронирования или владелец вещи могут получить данные о бронировании",
                exception.getMessage());
    }

    @Test
    void getUserBookingsShouldReturnAllBookings() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(bookingDao.findAllUserBookingByUserId(1L)).thenReturn(List.of(booking));

        List<BookingDto> result = (List<BookingDto>) bookingService.getUserBookings("ALL", 1L);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.getFirst().getId());
        assertEquals(booking.getItem().getId(), result.getFirst().getItem().getId());
    }

    @Test
    void getUserBookingsShouldThrowValidationExceptionWhenInvalidState() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getUserBookings("INVALID", 1L));
        assertEquals("Неверный параметр состояния: INVALID", exception.getMessage());
    }

    @Test
    void getOwnerBookingsShouldReturnBookingsWhenItemsExist() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(itemDao.findByOwnerId(1L)).thenReturn(List.of(item));
        when(bookingDao.findAllOwnerItemsBookingById(List.of(1L))).thenReturn(List.of(booking));

        List<BookingDto> result = (List<BookingDto>) bookingService.getOwnerBookings("ALL", 1L);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.getFirst().getId());
        assertEquals(booking.getItem().getId(), result.getFirst().getItem().getId());
    }

    @Test
    void getOwnerBookingsShouldReturnEmptyListWhenNoItems() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(itemDao.findByOwnerId(1L)).thenReturn(Collections.emptyList());

        List<BookingDto> result = (List<BookingDto>) bookingService.getOwnerBookings("ALL", 1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteBookingShouldDeleteWhenBooker() {
        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(bookingDao).deleteById(1L);

        bookingService.deleteBooking(1L, 1L);

        verify(bookingDao, times(1)).deleteById(1L);
    }

    @Test
    void deleteBookingShouldThrowValidationExceptionWhenNotBooker() {
        // Arrange
        User otherUser = new User(2L, "User2", "user2@example.com");
        when(bookingDao.findById(1L)).thenReturn(Optional.of(booking));
        when(userDao.findById(2L)).thenReturn(Optional.of(otherUser));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.deleteBooking(1L, 2L));
        assertEquals("Только создателя бронирования может его удалить", exception.getMessage());
        verify(bookingDao, never()).deleteById(any());
    }
}
