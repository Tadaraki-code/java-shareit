package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.dao.BookingDao;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dao.ItemDao;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BookingDaoTest {

    @Autowired
    private BookingDao bookingDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ItemDao itemDao;

    private User user;
    private User owner;
    private Item item;
    private Booking pastBooking;
    private Booking currentBooking;
    private Booking futureBooking;

    @BeforeEach
    void setUp() {
        user = new User(null, "User1", "user1@example.com");
        owner = new User(null, "Owner", "owner@example.com");

        user = userDao.save(user);
        owner = userDao.save(owner);

        item = new Item(null, "Item1", "Description", true, owner, null);
        item = itemDao.save(item);

        pastBooking = new Booking(null, LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1), item, user, Status.APPROVED);
        currentBooking = new Booking(null, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), item, user, Status.APPROVED);
        futureBooking = new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, user, Status.WAITING);

        pastBooking = bookingDao.save(pastBooking);
        currentBooking = bookingDao.save(currentBooking);
        futureBooking = bookingDao.save(futureBooking);
    }

    @Test
    void findAllUserBookingByUserIdShouldReturnAllBookings() {
        Collection<Booking> result = bookingDao.findAllUserBookingByUserId(user.getId());

        assertEquals(3, result.size());
        assertTrue(result.contains(pastBooking));
        assertTrue(result.contains(currentBooking));
        assertTrue(result.contains(futureBooking));
        List<Booking> sortedResult = result.stream()
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .toList();
        assertEquals(sortedResult, result);
    }

    @Test
    void findCurrentUserBookingByUserIdShouldReturnCurrentBookings() {
        LocalDateTime now = LocalDateTime.now();

        Collection<Booking> result = bookingDao.findCurrentUserBookingByUserId(user.getId(), now);

        assertEquals(1, result.size());
        assertTrue(result.contains(currentBooking));
        assertFalse(result.contains(pastBooking));
        assertFalse(result.contains(futureBooking));
    }

    @Test
    void findAllOwnerItemsBookingByIdShouldReturnBookingsForOwnerItems() {
        List<Long> itemIds = List.of(item.getId());

        Collection<Booking> result = bookingDao.findAllOwnerItemsBookingById(itemIds);

        assertEquals(3, result.size());
        assertTrue(result.contains(pastBooking));
        assertTrue(result.contains(currentBooking));
        assertTrue(result.contains(futureBooking));
        List<Booking> sortedResult = result.stream()
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .toList();
        assertEquals(sortedResult, result);
    }

    @Test
    void findPastOwnerItemsBookingByIdShouldReturnPastBookings() {
        List<Long> itemIds = List.of(item.getId());
        LocalDateTime now = LocalDateTime.now();

        Collection<Booking> result = bookingDao.findPastOwnerItemsBookingById(itemIds, now);

        assertEquals(1, result.size());
        assertTrue(result.contains(pastBooking));
        assertFalse(result.contains(currentBooking));
        assertFalse(result.contains(futureBooking));
        List<Booking> sortedResult = result.stream()
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .toList();
        assertEquals(sortedResult, result);
    }

    @Test
    void findBookingByUserIdAndItemIdShouldReturnEmptyWhenNotExists() {
        Optional<Booking> result = bookingDao.findBookingByUserIdAndItemId(999L, item.getId());

        assertFalse(result.isPresent());
    }
}
