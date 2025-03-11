package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.RequestBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    @Test
    void toBookingDtoShouldMapBookingToDtoCorrectly() {
        User booker = new User(1L, "User1", "user1@example.com");
        Item item = new Item(1L, "Item1", "Description", true, booker, null);
        Booking booking = new Booking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, Status.APPROVED);

        BookingDto result = BookingMapper.toBookingDto(booking);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getStatus(), result.getStatus());

        ItemDto itemDto = result.getItem();
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.isAvailable(), itemDto.getAvailable());
        assertNull(itemDto.getRequestId());

        UserDto userDto = result.getBooker();
        assertEquals(booker.getId(), userDto.getId());
        assertEquals(booker.getName(), userDto.getName());
        assertEquals(booker.getEmail(), userDto.getEmail());
    }

    @Test
    void fromBookingDtoShouldMapRequestBookingDtoToBookingCorrectly() {
        RequestBookingDto requestBookingDto = new RequestBookingDto(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        User booker = new User(1L, "User1", "user1@example.com");
        Item item = new Item(1L, "Item1", "Description", true, booker, null);


        Booking result = BookingMapper.fromBookingDto(requestBookingDto, booker, item);


        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(requestBookingDto.getStart(), result.getStart());
        assertEquals(requestBookingDto.getEnd(), result.getEnd());
        assertEquals(item, result.getItem());
        assertEquals(booker, result.getBooker());
        assertEquals(Status.WAITING, result.getStatus());
    }

    @Test
    void toBookingDto_ShouldHandleNullItemRequest() {
        User booker = new User(1L, "User1", "user1@example.com");
        Item item = new Item(1L, "Item1", "Description", true, booker, null);
        Booking booking = new Booking(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, Status.APPROVED);

        BookingDto result = BookingMapper.toBookingDto(booking);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStart(), result.getStart());
        assertEquals(booking.getEnd(), result.getEnd());
        assertEquals(booking.getStatus(), result.getStatus());

        ItemDto itemDto = result.getItem();
        assertEquals(item.getId(), itemDto.getId());
        assertNull(itemDto.getRequestId());
    }
}