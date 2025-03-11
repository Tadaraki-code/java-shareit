package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
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
class RequestDaoTest {

    @Autowired
    private ItemRequestDao itemRequestDao;

    @Autowired
    private UserDao userDao;

    private User user1;
    private User user2;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;

    @BeforeEach
    void setUp() {
        user1 = new User(null, "User1", "user1@example.com");
        user2 = new User(null, "User2", "user2@example.com");

        user1 = userDao.save(user1);
        user2 = userDao.save(user2);

        request1 = new ItemRequest(null, "Request 1", user1, LocalDateTime.now().minusDays(2));
        request2 = new ItemRequest(null, "Request 2", user1, LocalDateTime.now().minusDays(1));
        request3 = new ItemRequest(null, "Request 3", user2, LocalDateTime.now());

        request1 = itemRequestDao.save(request1);
        request2 = itemRequestDao.save(request2);
        request3 = itemRequestDao.save(request3);
    }

    @Test
    void findAllOwnerRequestShouldReturnRequestsForOwnerSortedByCreatedDesc() {
        Collection<ItemRequest> result = itemRequestDao.findAllOwnerRequest(user1.getId());

        assertEquals(2, result.size());
        List<ItemRequest> resultList = List.copyOf(result);
        assertEquals(request2.getId(), resultList.get(0).getId());
        assertEquals(request1.getId(), resultList.get(1).getId());
        assertTrue(resultList.get(0).getCreated().isAfter(resultList.get(1).getCreated()));
        assertFalse(result.contains(request3));
    }

    @Test
    void findAllOwnerRequestShouldReturnEmptyListWhenNoRequests() {
        Long nonExistentUserId = 999L;


        Collection<ItemRequest> result = itemRequestDao.findAllOwnerRequest(nonExistentUserId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByNotRequesterIdShouldReturnRequestsFromOtherUsersSortedByCreatedDesc() {
        Collection<ItemRequest> result = itemRequestDao.findAllByNotRequesterId(user1.getId());

        assertEquals(1, result.size());
        assertTrue(result.contains(request3));
        assertFalse(result.contains(request1));
        assertFalse(result.contains(request2));
    }

    @Test
    void findAllByNotRequesterIdShouldReturnAllRequestsWhenUserHasNoRequests() {
        User user3 = new User(null, "User3", "user3@example.com");
        user3 = userDao.save(user3);

        Collection<ItemRequest> result = itemRequestDao.findAllByNotRequesterId(user3.getId());

        assertEquals(3, result.size());
        List<ItemRequest> resultList = List.copyOf(result);
        assertEquals(request3.getId(), resultList.get(0).getId()); // Самый новый
        assertEquals(request2.getId(), resultList.get(1).getId());
        assertEquals(request1.getId(), resultList.get(2).getId());
        assertTrue(resultList.get(0).getCreated().isAfter(resultList.get(1).getCreated()));
        assertTrue(resultList.get(1).getCreated().isAfter(resultList.get(2).getCreated()));
    }
}
