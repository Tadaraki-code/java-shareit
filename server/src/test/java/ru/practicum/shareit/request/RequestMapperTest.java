package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItem;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {

    @Test
    void toItemRequestDtoShouldMapCorrectly() {
        User requestor = new User(1L, "User1", "user1@example.com");
        ItemRequest itemRequest = new ItemRequest(1L, "Need a tool", requestor, LocalDateTime.now());

        ItemRequestDto result = ItemRequestMapper.toItemRequestDto(itemRequest);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
    }

    @Test
    void fromItemRequestDtoShouldMapCorrectly() {
        User requestor = new User(1L, "User1", "user1@example.com");
        ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "Need a tool",
                LocalDateTime.now().minusDays(1));

        LocalDateTime before = LocalDateTime.now();

        ItemRequest result = ItemRequestMapper.fromItemRequestDto(itemRequestDto, requestor);

        assertNotNull(result);
        assertEquals(itemRequestDto.getId(), result.getId());
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        assertEquals(requestor, result.getRequestor());
        assertTrue(result.getCreated().isAfter(before) || result.getCreated().isEqual(before));
        assertTrue(result.getCreated().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void toItemRequestWithItemShouldMapWithItemsCorrectly() {
        User requestor = new User(1L, "User1", "user1@example.com");
        ItemRequest itemRequest = new ItemRequest(1L, "Need a tool", requestor, LocalDateTime.now());
        Item item = new Item(1L, "Tool", "Tool description", true, requestor, itemRequest);
        List<Item> items = List.of(item);

        ItemRequestDtoWithItem result = ItemRequestMapper.toItemRequestWithItem(itemRequest, items);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertEquals(1, result.getItems().size());

        ItemDtoForRequest itemDto = result.getItems().getFirst();
        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(requestor.getId(), itemDto.getOwnerId());
    }

    @Test
    void toItemRequestWithItemShouldReturnEmptyItemsWhenItemsNull() {
        User requestor = new User(1L, "User1", "user1@example.com");
        ItemRequest itemRequest = new ItemRequest(1L, "Need a tool", requestor, LocalDateTime.now());

        ItemRequestDtoWithItem result = ItemRequestMapper.toItemRequestWithItem(itemRequest, null);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void toItemRequestWithItemShouldReturnEmptyItemsWhenItemsEmpty() {
        User requestor = new User(1L, "User1", "user1@example.com");
        ItemRequest itemRequest = new ItemRequest(1L, "Need a tool", requestor, LocalDateTime.now());

        ItemRequestDtoWithItem result = ItemRequestMapper.toItemRequestWithItem(itemRequest, Collections.emptyList());

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertTrue(result.getItems().isEmpty());
    }
}
