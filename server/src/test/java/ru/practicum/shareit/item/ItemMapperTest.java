package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.dto.ItemDtoWhitBooking;
import ru.practicum.shareit.item.dto.ItemDtoWhitComments;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    private User owner;
    private User booker;
    private Item item;
    private ItemRequest request;
    private Booking lastBooking;
    private Booking nextBooking;
    private Comment comment;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@example.com");
        booker = new User(2L, "Booker", "booker@example.com");
        request = new ItemRequest(1L, "Request", booker, LocalDateTime.now());
        item = new Item(1L, "Item", "Description", true, owner, request);
        lastBooking = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                item, booker, Status.APPROVED);
        nextBooking = new Booking(2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, Status.APPROVED);
        comment = new Comment(1L, "Great item!", item, booker, LocalDateTime.now().minusHours(1));
    }

    @Test
    void toItemDtoShouldMapItemWithRequest() {
        ItemDto result = ItemMapper.toItemDto(item);

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());
        assertEquals(request.getId(), result.getRequestId());
    }

    @Test
    void toItemDtoShouldMapItemWithoutRequest() {
        Item itemWithoutRequest = new Item(1L, "Item",
                "Description", true, owner, null);
        ItemDto result = ItemMapper.toItemDto(itemWithoutRequest);

        assertEquals(itemWithoutRequest.getId(), result.getId());
        assertEquals(itemWithoutRequest.getName(), result.getName());
        assertEquals(itemWithoutRequest.getDescription(), result.getDescription());
        assertEquals(itemWithoutRequest.isAvailable(), result.getAvailable());
        assertNull(result.getRequestId());
    }

    @Test
    void toItemDtoWhitBookingShouldMapWithBookingsAndComments() {
        ItemDtoWhitBooking result = ItemMapper.toItemDtoWhitBooking(item, lastBooking, nextBooking, List.of(comment));

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());

        assertNotNull(result.getLastBooking());
        assertEquals(lastBooking.getId(), result.getLastBooking().getId());
        assertEquals(lastBooking.getStart().format(DATE_TIME_FORMATTER),
                result.getLastBooking().getStart().format(DATE_TIME_FORMATTER));
        assertEquals(lastBooking.getEnd().format(DATE_TIME_FORMATTER),
                result.getLastBooking().getEnd().format(DATE_TIME_FORMATTER));

        assertNotNull(result.getNextBooking());
        assertEquals(nextBooking.getId(), result.getNextBooking().getId());
        assertEquals(nextBooking.getStart().format(DATE_TIME_FORMATTER),
                result.getNextBooking().getStart().format(DATE_TIME_FORMATTER));
        assertEquals(nextBooking.getEnd().format(DATE_TIME_FORMATTER),
                result.getNextBooking().getEnd().format(DATE_TIME_FORMATTER));

        assertEquals(1, result.getComments().size());
        assertTrue(result.getComments().stream()
                .anyMatch(c -> c.getText().equals(comment.getText()) &&
                        c.getAuthorName().equals(booker.getName()) &&
                        c.getCreated().format(DATE_TIME_FORMATTER)
                                .equals(comment.getCreated().format(DATE_TIME_FORMATTER))));
    }

    @Test
    void toItemDtoWhitBookingShouldMapWithoutBookings() {
        ItemDtoWhitBooking result = ItemMapper.toItemDtoWhitBooking(item, null,
                null, Collections.emptyList());

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void fromItemDtoShouldMapToItemWithRequest() {
        ItemDto itemDto = new ItemDto(1L, "Item", "Description", true, 1L);
        Item result = ItemMapper.fromItemDto(itemDto, owner, request);

        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.isAvailable());
        assertEquals(owner, result.getOwner());
        assertEquals(request, result.getRequest());
    }

    @Test
    void fromItemDtoShouldMapToItemWithoutRequest() {
        ItemDto itemDto = new ItemDto(1L, "Item", "Description", true, null);
        Item result = ItemMapper.fromItemDto(itemDto, owner, null);

        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.isAvailable());
        assertEquals(owner, result.getOwner());
        assertNull(result.getRequest());
    }

    @Test
    void toItemDtoWhitCommentsShouldMapWithComments() {
        ItemDtoWhitComments result = ItemMapper.toItemDtoWhitComments(item, List.of(comment));

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertEquals(1, result.getComments().size());
        assertTrue(result.getComments().stream()
                .anyMatch(c -> c.getText().equals(comment.getText()) &&
                        c.getAuthorName().equals(booker.getName()) &&
                        c.getCreated().format(DATE_TIME_FORMATTER)
                                .equals(comment.getCreated().format(DATE_TIME_FORMATTER))));
    }

    @Test
    void toItemDtoWhitCommentsShouldMapWithoutComments() {
        ItemDtoWhitComments result = ItemMapper.toItemDtoWhitComments(item, Collections.emptyList());

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void toItemDtoForRequestShouldMapItem() {
        ItemDtoForRequest result = ItemMapper.toItemDtoForRequest(item);

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(owner.getId(), result.getOwnerId());
    }
}
