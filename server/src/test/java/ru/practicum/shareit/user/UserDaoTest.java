package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    void findByIdShouldReturnUserWhenUserExists() {
        User user = new User(null, "User1", "user1@example.com");
        User savedUser = userDao.save(user);

        Optional<User> result = userDao.findById(savedUser.getId());

        assertTrue(result.isPresent());
        assertEquals(savedUser.getId(), result.get().getId());
        assertEquals("User1", result.get().getName());
        assertEquals("user1@example.com", result.get().getEmail());
    }

    @Test
    void findByIdShouldReturnEmptyWhenUserDoesNotExist() {
        Optional<User> result = userDao.findById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void findAllShouldReturnAllUsers() {
        User user1 = new User(null, "User1", "user1@example.com");
        User user2 = new User(null, "User2", "user2@example.com");
        userDao.save(user1);
        userDao.save(user2);

        List<User> result = userDao.findAll();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getEmail().equals("user1@example.com")));
        assertTrue(result.stream().anyMatch(u -> u.getEmail().equals("user2@example.com")));
    }

    @Test
    void findAllShouldReturnEmptyListWhenNoUsers() {
        List<User> result = userDao.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void saveShouldPersistUser() {
        User user = new User(null, "User1", "user1@example.com");

        User savedUser = userDao.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("User1", savedUser.getName());
        assertEquals("user1@example.com", savedUser.getEmail());
    }

    @Test
    void deleteByIdShouldRemoveUser() {
        User user = new User(null, "User1", "user1@example.com");
        User savedUser = userDao.save(user);

        userDao.deleteById(savedUser.getId());
        Optional<User> result = userDao.findById(savedUser.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void existsByEmailShouldReturnTrueWhenEmailExists() {
        User user = new User(null, "User1", "user1@example.com");
        userDao.save(user);

        boolean exists = userDao.existsByEmail("user1@example.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmailShouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists = userDao.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    void existsByEmailAndIdNotShouldReturnTrueWhenEmailExistsForOtherUser() {
        User user1 = new User(null, "User1", "user1@example.com");
        User user2 = new User(null, "User2", "user2@example.com");
        User savedUser1 = userDao.save(user1);
        userDao.save(user2);

        boolean exists = userDao.existsByEmailAndIdNot("user1@example.com", savedUser1.getId() + 1);

        assertTrue(exists);
    }

    @Test
    void existsByEmailAndIdNotShouldReturnFalseWhenEmailDoesNotExist() {
        User user = new User(null, "User1", "user1@example.com");
        User savedUser = userDao.save(user);

        boolean exists = userDao.existsByEmailAndIdNot("nonexistent@example.com", savedUser.getId());

        assertFalse(exists);
    }

    @Test
    void existsByEmailAndIdNotShouldReturnFalseWhenEmailBelongsToSameUser() {
        User user = new User(null, "User1", "user1@example.com");
        User savedUser = userDao.save(user);

        boolean exists = userDao.existsByEmailAndIdNot("user1@example.com", savedUser.getId());

        assertFalse(exists);
    }
}
