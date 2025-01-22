package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.Map;

public interface UserService {

    UserDto getUser(Long id);

    Collection<UserDto> getAllUser();

    UserDto createUser(UserDto userDto);

    void deleteUser(Long id);

    UserDto updateUser(Map<String, String> update, Long id);
}
