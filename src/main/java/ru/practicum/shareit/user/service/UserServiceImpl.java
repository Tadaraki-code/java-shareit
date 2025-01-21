package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Override
    public User getUser(Long id) {
        return userDao.getUser(id);
    }

    @Override
    public Collection<User> getAllUser() {
        return userDao.getAllUser();
    }

    @Override
    public User createUser(User user) {
        return userDao.createUser(user);
    }

    @Override
    public void deleteUser(Long id) {
        userDao.deleteUser(id);
    }

    @Override
    public User updateUser(Map<String, String> update, Long id) {
        return userDao.updateUser(update, id);
    }
}
