package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserService {

    User getUser(Long id);

    Collection<User> getAllUser();

    User createUser(User user);

    void deleteUser(Long id);

    User updateUser(Map<String, String> update, Long id);
}
