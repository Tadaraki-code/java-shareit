package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestDao;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ItemDaoTest {

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ItemRequestDao itemRequestDao;

    private User owner;
    private User requester;
    private ItemRequest request;
    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        owner = userDao.save(new User(null, "Owner", "owner@example.com"));
        requester = userDao.save(new User(null, "Requester", "requester@example.com"));

        request = itemRequestDao.save(new ItemRequest(null, "Request", requester, LocalDateTime.now()));

        item1 = itemDao.save(new Item(null, "Item1", "Description1",
                true, owner, null));
        item2 = itemDao.save(new Item(null, "Item2",
                "Description2 item", true, owner, request));
    }

    @Test
    void findByNameContainingOrDescriptionContainingShouldReturnItemsWhenTextMatches() {
        Collection<Item> result = itemDao.findByNameContainingOrDescriptionContaining("item");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item1")));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item2")));
    }

    @Test
    void findByNameContainingOrDescriptionContainingShouldReturnEmptyListWhenNoMatch() {
        Collection<Item> result = itemDao.findByNameContainingOrDescriptionContaining("xyz");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByNameContainingOrDescriptionContainingShouldReturnOnlyAvailableItems() {
        Item unavailableItem = itemDao.save(new Item(null, "Item3",
                "Description3", false, owner, null));

        Collection<Item> result = itemDao.findByNameContainingOrDescriptionContaining("item");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(Item::isAvailable));
    }

    @Test
    void findByOwnerIdShouldReturnItemsWhenOwnerExists() {
        Collection<Item> result = itemDao.findByOwnerId(owner.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item1")));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item2")));
    }

    @Test
    void findByOwnerIdShouldReturnEmptyListWhenNoItemsForOwner() {
        User otherOwner = userDao.save(new User(null, "Other", "other@example.com"));
        Collection<Item> result = itemDao.findByOwnerId(otherOwner.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllItemsByItemRequestIdShouldReturnItems_WhenRequestExists() {
        Collection<Item> result = itemDao.findAllItemsByItemRequestId(request.getId());

        assertEquals(1, result.size());
        assertEquals("Item2", result.iterator().next().getName());
    }

    @Test
    void findAllItemsByItemRequestIdShouldReturnEmptyListWhenNoItemsForRequest() {
        ItemRequest otherRequest = itemRequestDao.save(new ItemRequest(null,
                "Other Request", requester, LocalDateTime.now()));
        Collection<Item> result = itemDao.findAllItemsByItemRequestId(otherRequest.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllItemsByItemsRequestIdsShouldReturnItems_WhenRequestsExist() {
        Collection<Item> result = itemDao.findAllItemsByItemsRequestIds(List.of(request.getId()));

        assertEquals(1, result.size());
        assertEquals("Item2", result.iterator().next().getName());
    }

    @Test
    void findAllItemsByItemsRequestIdsShouldReturnEmptyListWhenNoItemsForRequests() {
        ItemRequest otherRequest = itemRequestDao.save(new ItemRequest(null,
                "Other Request", requester, LocalDateTime.now()));
        Collection<Item> result = itemDao.findAllItemsByItemsRequestIds(List.of(otherRequest.getId()));

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllItemsByItemsRequestIdsShouldReturnMultipleItems_WhenMultipleRequests() {
        ItemRequest request2 = itemRequestDao.save(new ItemRequest(null,
                "Request2", requester, LocalDateTime.now()));
        Item item3 = itemDao.save(new Item(null, "Item3",
                "Description3", true, owner, request2));

        Collection<Item> result = itemDao.findAllItemsByItemsRequestIds(List.of(request.getId(), request2.getId()));

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item2")));
        assertTrue(result.stream().anyMatch(item -> item.getName().equals("Item3")));
    }
}
