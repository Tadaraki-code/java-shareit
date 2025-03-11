package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestDao;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItem;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private ItemRequestDao itemRequestDao;

    @Mock
    private ItemDao itemDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User1", "user1@example.com");
        itemRequest = new ItemRequest(1L, "Need a tool", user, LocalDateTime.now());
        itemRequestDto = new ItemRequestDto(null, "Need a tool", null);
        item = new Item(1L, "Tool", "Tool description", true, user, itemRequest);
    }

    @Test
    void createItemRequestShouldReturnDtoWhenValid() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestDao.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.createItemRequest(itemRequestDto, 1L);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        verify(userDao, times(1)).findById(1L);
        verify(itemRequestDao, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void createItemRequestShouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.createItemRequest(itemRequestDto, 1L));
        assertEquals("Пользователь c id 1 не найден.", exception.getMessage());
        verify(itemRequestDao, never()).save(any());
    }

    @Test
    void getItemRequestShouldReturnDtoWithItemsWhenValid() {
        when(itemRequestDao.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemDao.findAllItemsByItemRequestId(1L)).thenReturn(List.of(item));

        ItemRequestDtoWithItem result = itemRequestService.getItemRequest(1L);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertEquals(1, result.getItems().size());
        assertEquals(item.getId(), result.getItems().getFirst().getId());
        assertEquals(item.getName(), result.getItems().getFirst().getName());
        assertEquals(user.getId(), result.getItems().getFirst().getOwnerId());
        verify(itemRequestDao, times(1)).findById(1L);
        verify(itemDao, times(1)).findAllItemsByItemRequestId(1L);
    }

    @Test
    void getItemRequestShouldReturnDtoWithEmptyItemsWhenNoItems() {
        when(itemRequestDao.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemDao.findAllItemsByItemRequestId(1L)).thenReturn(Collections.emptyList());

        ItemRequestDtoWithItem result = itemRequestService.getItemRequest(1L);

        assertNotNull(result);
        assertEquals(itemRequest.getId(), result.getId());
        assertEquals(itemRequest.getDescription(), result.getDescription());
        assertEquals(itemRequest.getCreated(), result.getCreated());
        assertTrue(result.getItems().isEmpty());
        verify(itemRequestDao, times(1)).findById(1L);
        verify(itemDao, times(1)).findAllItemsByItemRequestId(1L);
    }

    @Test
    void getAllOwnerRequestShouldReturnRequestsWithItemsWhenValid() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestDao.findAllOwnerRequest(1L)).thenReturn(List.of(itemRequest));
        when(itemDao.findAllItemsByItemsRequestIds(List.of(1L))).thenReturn(List.of(item));

        List<ItemRequestDtoWithItem> result = (List<ItemRequestDtoWithItem>) itemRequestService
                .getAllOwnerRequest(1L);

        assertEquals(1, result.size());
        ItemRequestDtoWithItem dto = result.getFirst();
        assertEquals(itemRequest.getId(), dto.getId());
        assertEquals(itemRequest.getDescription(), dto.getDescription());
        assertEquals(itemRequest.getCreated(), dto.getCreated());
        assertEquals(1, dto.getItems().size());
        assertEquals(item.getId(), dto.getItems().getFirst().getId());
        assertEquals(item.getName(), dto.getItems().getFirst().getName());
        assertEquals(user.getId(), dto.getItems().getFirst().getOwnerId());
        verify(userDao, times(1)).findById(1L);
        verify(itemRequestDao, times(1)).findAllOwnerRequest(1L);
        verify(itemDao, times(1)).findAllItemsByItemsRequestIds(List.of(1L));
    }

    @Test
    void getAllOwnerRequestShouldReturnEmptyListWhenNoRequests() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestDao.findAllOwnerRequest(1L)).thenReturn(Collections.emptyList());
        when(itemDao.findAllItemsByItemsRequestIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ItemRequestDtoWithItem> result = (List<ItemRequestDtoWithItem>) itemRequestService
                .getAllOwnerRequest(1L);

        assertTrue(result.isEmpty());
        verify(userDao, times(1)).findById(1L);
        verify(itemRequestDao, times(1)).findAllOwnerRequest(1L);
        verify(itemDao, times(1)).findAllItemsByItemsRequestIds(Collections.emptyList());
    }

    @Test
    void getAllRequestShouldReturnAllRequestsWhenValid() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestDao.findAllByNotRequesterId(1L)).thenReturn(List.of(itemRequest));

        List<ItemRequestDto> result = (List<ItemRequestDto>) itemRequestService.getAllRequest(1L);

        assertEquals(1, result.size());
        assertEquals(itemRequest.getId(), result.getFirst().getId());
        assertEquals(itemRequest.getDescription(), result.getFirst().getDescription());
        assertEquals(itemRequest.getCreated(), result.getFirst().getCreated());
        verify(userDao, times(1)).findById(1L);
        verify(itemRequestDao, times(1)).findAllByNotRequesterId(1L);
    }

    @Test
    void deleteItemRequestShouldDeleteWhenValid() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestDao.findById(1L)).thenReturn(Optional.of(itemRequest));
        doNothing().when(itemRequestDao).deleteById(1L);

        itemRequestService.deleteItemRequest(1L, 1L);

        verify(userDao, times(1)).findById(1L);
        verify(itemRequestDao, times(1)).findById(1L);
        verify(itemRequestDao, times(1)).deleteById(1L);
    }

    @Test
    void deleteItemRequestShouldThrowNotFoundExceptionWhenRequestNotFound() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestDao.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemRequestService.deleteItemRequest(1L, 1L));
        assertEquals("Запрос c id 1 не найден.", exception.getMessage());
        verify(itemRequestDao, never()).deleteById(any());
    }
}
