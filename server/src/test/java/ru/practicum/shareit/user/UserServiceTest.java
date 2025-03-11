package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dao.UserDao;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User1", "user1@example.com");
        userDto = UserMapper.toUserDto(user);
    }

    @Test
    void getUserShouldReturnUserDtoWhenUserExists() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    void getUserShouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUser(1L));
        assertEquals("Пользователь не найден.", exception.getMessage());
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    void getAllUserShouldReturnListOfUserDtos() {
        List<User> users = List.of(user);
        when(userDao.findAll()).thenReturn(users);

        List<UserDto> result = (List<UserDto>) userService.getAllUser();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userDto.getId(), result.getFirst().getId());
        assertEquals(userDto.getName(), result.getFirst().getName());
        assertEquals(userDto.getEmail(), result.getFirst().getEmail());
        verify(userDao, times(1)).findAll();
    }

    @Test
    void getAllUserShouldReturnEmptyListWhenNoUsers() {
        when(userDao.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = (List<UserDto>) userService.getAllUser();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userDao, times(1)).findAll();
    }

    @Test
    void createUserShouldReturnCreatedUserDto() {
        UserDto inputDto = new UserDto(null, "User2", "user2@example.com");
        User newUser = UserMapper.fromUserDto(inputDto);
        User savedUser = new User(2L, "User2", "user2@example.com");
        when(userDao.existsByEmail("user2@example.com")).thenReturn(false);
        when(userDao.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.createUser(inputDto);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("User2", result.getName());
        assertEquals("user2@example.com", result.getEmail());
        verify(userDao, times(1)).existsByEmail("user2@example.com");
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void createUserShouldThrowAlreadyExistExceptionWhenEmailExists() {
        UserDto inputDto = new UserDto(null, "User2", "user1@example.com");
        when(userDao.existsByEmail("user1@example.com")).thenReturn(true);

        AlreadyExistException exception = assertThrows(AlreadyExistException.class, () -> userService
                .createUser(inputDto));
        assertEquals("Пользователь с таким email уже существует.", exception.getMessage());
        verify(userDao, times(1)).existsByEmail("user1@example.com");
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void deleteUserShouldCallDeleteById() {
        doNothing().when(userDao).deleteById(1L);

        userService.deleteUser(1L);

        verify(userDao, times(1)).deleteById(1L);
    }

    @Test
    void updateUserShouldUpdateNameAndReturnUserDto() {
        Map<String, String> update = new HashMap<>();
        update.put("name", "Updated User");
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.save(any(User.class))).thenReturn(new User(1L, "Updated User",
                "user1@example.com"));

        UserDto result = userService.updateUser(update, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated User", result.getName());
        assertEquals("user1@example.com", result.getEmail());
        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void updateUserShouldUpdateEmailAndReturnUserDto() {
        Map<String, String> update = new HashMap<>();
        update.put("email", "updated@example.com");
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.existsByEmailAndIdNot("updated@example.com", 1L)).thenReturn(false);
        when(userDao.save(any(User.class))).thenReturn(new User(1L, "User1", "updated@example.com"));

        UserDto result = userService.updateUser(update, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("User1", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).existsByEmailAndIdNot("updated@example.com", 1L);
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    void updateUserShouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        Map<String, String> update = new HashMap<>();
        update.put("name", "Updated User");
        when(userDao.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService
                .updateUser(update, 1L));
        assertEquals("Пользователь не найден.", exception.getMessage());
        verify(userDao, times(1)).findById(1L);
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void updateUserShouldThrowValidationExceptionWhenNameIsBlank() {
        Map<String, String> update = new HashMap<>();
        update.put("name", " ");
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class, () -> userService
                .updateUser(update, 1L));
        assertEquals("В запросе на обновление имени была передана пустая строчка", exception.getMessage());
        verify(userDao, times(1)).findById(1L);
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void updateUserShouldThrowValidationExceptionWhenEmailIsBlank() {
        Map<String, String> update = new HashMap<>();
        update.put("email", " ");
        when(userDao.findById(1L)).thenReturn(Optional.of(user));

        ValidationException exception = assertThrows(ValidationException.class, () -> userService
                .updateUser(update, 1L));
        assertEquals("В запросе на обновление имейла была передана пустая строчка", exception.getMessage());
        verify(userDao, times(1)).findById(1L);
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void updateUserShouldThrowAlreadyExistExceptionWhenEmailExists() {
        Map<String, String> update = new HashMap<>();
        update.put("email", "user2@example.com");
        when(userDao.findById(1L)).thenReturn(Optional.of(user));
        when(userDao.existsByEmailAndIdNot("user2@example.com", 1L)).thenReturn(true);

        AlreadyExistException exception = assertThrows(AlreadyExistException.class, () -> userService
                .updateUser(update, 1L));
        assertEquals("Пользователь с email user2@example.com уже существует", exception.getMessage());
        verify(userDao, times(1)).findById(1L);
        verify(userDao, times(1)).existsByEmailAndIdNot("user2@example.com", 1L);
        verify(userDao, never()).save(any(User.class));
    }
}
