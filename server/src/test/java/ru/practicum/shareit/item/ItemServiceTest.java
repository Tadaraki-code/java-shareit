package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dao.BookingDao;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dao.CommentDao;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWhitBooking;
import ru.practicum.shareit.item.dto.ItemDtoWhitComments;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.dao.ItemRequestDao;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemDao itemDao;

    @Mock
    private UserDao userDao;

    @Mock
    private BookingDao bookingDao;

    @Mock
    private CommentDao commentDao;

    @Mock
    private ItemRequestDao itemRequestDao;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking pastBooking;
    private Booking futureBooking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@example.com");
        booker = new User(2L, "Booker", "booker@example.com");
        item = new Item(1L, "Item", "Description", true, owner, null);
        pastBooking = new Booking(1L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                item, booker, Status.APPROVED);
        futureBooking = new Booking(2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, Status.APPROVED);
        comment = new Comment(1L, "Great item!", item, booker, LocalDateTime.now().minusHours(1));
    }

    @Test
    void getItemShouldReturnItemDtoWhitComments() {
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));
        when(commentDao.findAllCommentsByItemId(1L)).thenReturn(List.of(comment));

        ItemDtoWhitComments result = itemService.getItem(1L);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertEquals(1, result.getComments().size());
        assertEquals(comment.getText(), result.getComments().iterator().next().getText());
        assertEquals(booker.getName(), result.getComments().iterator().next().getAuthorName());
        verify(itemDao, times(1)).findById(1L);
        verify(commentDao, times(1)).findAllCommentsByItemId(1L);
    }

    @Test
    void getItemShouldThrowNotFoundExceptionWhenItemNotExists() {
        when(itemDao.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemService.getItem(1L));
        assertEquals("Вещь с id 1 не найдена", exception.getMessage());
    }

    @Test
    void getAllOwnerItemsShouldReturnListWhenItemsExist() {
        when(userDao.findById(1L)).thenReturn(Optional.of(owner));
        when(itemDao.findByOwnerId(1L)).thenReturn(List.of(item));
        when(bookingDao.findAllRelevantApprovedOwnerItemsBookingsById(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(pastBooking, futureBooking));
        when(commentDao.findAllCommentsForAllItemsById(anyList())).thenReturn(List.of(comment));

        List<ItemDtoWhitBooking> result = itemService.getAllOwnerItems(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(item.getId(), result.getFirst().getId());
        assertNotNull(result.getFirst().getLastBooking());
        assertEquals(pastBooking.getId(), result.getFirst().getLastBooking().getId());
        assertNotNull(result.getFirst().getNextBooking());
        assertEquals(futureBooking.getId(), result.getFirst().getNextBooking().getId());
        assertEquals(1, result.getFirst().getComments().size());
        verify(itemDao, times(1)).findByOwnerId(1L);
    }

    @Test
    void getAllOwnerItemsShouldReturnEmptyListWhenNoItems() {
        when(userDao.findById(1L)).thenReturn(Optional.of(owner));
        when(itemDao.findByOwnerId(1L)).thenReturn(Collections.emptyList());

        List<ItemDtoWhitBooking> result = itemService.getAllOwnerItems(1L);

        assertTrue(result.isEmpty());
        verify(itemDao, times(1)).findByOwnerId(1L);
        verify(bookingDao, never()).findAllRelevantApprovedOwnerItemsBookingsById(anyList(), any());
    }

    @Test
    void createItemShouldReturnItemDtoWhenNoRequest() {
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, null);
        when(userDao.findById(1L)).thenReturn(Optional.of(owner));
        when(itemDao.save(any(Item.class))).thenReturn(item);

        ItemDto result = itemService.createItem(itemDto, 1L);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.isAvailable(), result.getAvailable());
        assertNull(result.getRequestId());
        verify(itemDao, times(1)).save(any(Item.class));
    }

    @Test
    void createItemShouldReturnItemDtoWhenRequestExists() {
        ItemRequest request = new ItemRequest(1L, "Request", booker, LocalDateTime.now());
        Item itemWithRequest = new Item(1L, "Item", "Description", true, owner, request);
        ItemDto itemDto = new ItemDto(null, "Item", "Description", true, 1L);
        when(userDao.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestDao.findById(1L)).thenReturn(Optional.of(request));
        when(itemDao.save(any(Item.class))).thenReturn(itemWithRequest);

        ItemDto result = itemService.createItem(itemDto, 1L);

        assertNotNull(result);
        assertEquals(itemWithRequest.getId(), result.getId());
        assertEquals(1L, result.getRequestId());
        verify(itemRequestDao, times(1)).findById(1L);
        verify(itemDao, times(1)).save(any(Item.class));
    }

    @Test
    void deleteItemShouldDeleteWhenOwner() {
        when(userDao.findById(1L)).thenReturn(Optional.of(owner));
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));

        itemService.deleteItem(1L, 1L);

        verify(itemDao, times(1)).deleteById(1L);
    }

    @Test
    void deleteItemShouldThrowValidationExceptionWhenNotOwner() {
        when(userDao.findById(2L)).thenReturn(Optional.of(booker));
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.deleteItem(1L, 2L));
        assertEquals("Только владелец вещи может удалить вещь", exception.getMessage());
    }

    @Test
    void updateItemShouldUpdateFields() {
        Map<String, String> update = new HashMap<>();
        update.put("name", "New Name");
        update.put("description", "New Desc");
        update.put("available", "false");
        Item updatedItem = new Item(1L, "New Name", "New Desc", false, owner, null);
        when(userDao.findById(1L)).thenReturn(Optional.of(owner));
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));
        when(itemDao.save(any(Item.class))).thenReturn(updatedItem);

        ItemDto result = itemService.updateItem(update, 1L, 1L);

        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertFalse(result.getAvailable());
        verify(itemDao, times(1)).save(any(Item.class));
    }

    @Test
    void updateItemShouldThrowValidationExceptionWhenBlankName() {
        Map<String, String> update = new HashMap<>();
        update.put("name", "");
        when(userDao.findById(1L)).thenReturn(Optional.of(owner));
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.updateItem(update, 1L, 1L));
        assertEquals("В запросе на обновление названия вещи была передана пустая строчка.",
                exception.getMessage());
    }

    @Test
    void searchItemsShouldReturnEmptyListWhenTextBlank() {
        List<ItemDto> result = itemService.searchItems("");

        assertTrue(result.isEmpty());
        verify(itemDao, never()).findByNameContainingOrDescriptionContaining(anyString());
    }

    @Test
    void searchItemsShouldReturnItemsWhenTextValid() {
        when(itemDao.findByNameContainingOrDescriptionContaining("item")).thenReturn(List.of(item));

        List<ItemDto> result = itemService.searchItems("item");

        assertEquals(1, result.size());
        assertEquals(item.getId(), result.getFirst().getId());
        assertEquals(item.getName(), result.getFirst().getName());
        verify(itemDao, times(1)).findByNameContainingOrDescriptionContaining("item");
    }

    @Test
    void createCommentShouldReturnCommentDtoWhenValid() {
        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));
        when(userDao.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingDao.findBookingByUserIdAndItemId(2L, 1L)).thenReturn(Optional.of(pastBooking));
        when(commentDao.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = itemService.createComment(commentDto, 1L, 2L);

        assertNotNull(result);
        assertEquals(comment.getText(), result.getText());
        assertEquals(booker.getName(), result.getAuthorName());
        verify(commentDao, times(1)).save(any(Comment.class));
    }

    @Test
    void createCommentShouldThrowValidationExceptionWhenBookingNotEnded() {
        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));
        when(userDao.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingDao.findBookingByUserIdAndItemId(2L, 1L)).thenReturn(Optional.of(futureBooking));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.createComment(commentDto, 1L, 2L));
        assertEquals("Вы не можете оставить отзыв о вещи с id 1, " +
                        "поскольку вы не брали её в аренду или срок аренды ещё не истёк.",
                exception.getMessage());
    }

    @Test
    void createCommentShouldThrowNotFoundExceptionWhenNoBooking() {
        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);
        when(itemDao.findById(1L)).thenReturn(Optional.of(item));
        when(userDao.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingDao.findBookingByUserIdAndItemId(2L, 1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createComment(commentDto, 1L, 2L));
        assertEquals("Бронь не найден", exception.getMessage());
    }
}
