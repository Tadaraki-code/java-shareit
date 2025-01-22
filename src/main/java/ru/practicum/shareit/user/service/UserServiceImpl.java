package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    @Override
    public UserDto getUser(Long id) {
        log.info("Передаём запрос на получение пользователя в userDao.");
        return UserMapper.toUserDto(userDao.getUser(id));
    }

    @Override
    public Collection<UserDto> getAllUser() {
        log.info("Передаём запрос на список всех пользователей в userDao.");
        return userDao.getAllUser().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Передаём запрос на создание нового пользователя с id {} в userDao.", userDto);
        return UserMapper.toUserDto(userDao.createUser(UserMapper.fromUserDto(userDto)));
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Передаём запрос на удаление пользоватля с id {} в userDao.", id);
        userDao.deleteUser(id);
    }

    @Override
    public UserDto updateUser(Map<String, String> update, Long id) {
        log.info("Передаём запрос на обновление пользоватля с id {} в userDao.", id);
        return UserMapper.toUserDto(userDao.updateUser(update, id));
    }
}
