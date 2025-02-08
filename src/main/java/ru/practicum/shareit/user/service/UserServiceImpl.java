package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.ValidationException;

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
        User user = userDao.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> getAllUser() {
        log.info("Передаём запрос на список всех пользователей в userDao.");
        return userDao.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Передаём запрос на создание нового пользователя с email {}", userDto.getEmail());

        if (userDao.existsByEmail(userDto.getEmail())) {
            throw new AlreadyExistException("Пользователь с таким email уже существует.");
        }

        return UserMapper.toUserDto(userDao.save(UserMapper.fromUserDto(userDto)));
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Передаём запрос на удаление пользоватля с id {} в userDao.", id);
        userDao.deleteById(id);
    }

    @Override
    public UserDto updateUser(Map<String, String> update, Long id) {
        log.info("Передаём запрос на обновление пользоватля с id {} в userDao.", id);
        User oldUser = userDao.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден."));

        if (update.get("name") != null) {
            if (!update.get("name").isBlank()) {
                oldUser.setName(update.get("name"));
            } else {
                throw new ValidationException("В запросе на обновление имени была передана пустая строчка");
            }
        }
        if (update.get("email") != null) {
            if (update.get("email").isBlank()) {
                throw new ValidationException("В запросе на обновление имейла была передана пустая строчка");
            }
            if (!userDao.existsByEmailAndIdNot(update.get("email"), id)) {
                oldUser.setEmail(update.get("email"));
            } else {
                throw new AlreadyExistException("Пользователь с email " + update.get("email") + " уже существует");
            }
        }
        userDao.save(oldUser);
        return UserMapper.toUserDto(oldUser);
    }

}
