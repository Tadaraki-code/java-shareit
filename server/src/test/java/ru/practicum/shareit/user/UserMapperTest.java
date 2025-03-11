package ru.practicum.shareit.user;


import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toUserDtoShouldMapUserToUserDto() {
        User user = new User(1L, "User1", "user1@example.com");

        UserDto result = UserMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("User1", result.getName());
        assertEquals("user1@example.com", result.getEmail());
    }

    @Test
    void toUserDtoShouldHandleNullFields() {
        User user = new User(1L, null, null);

        UserDto result = UserMapper.toUserDto(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getName());
        assertNull(result.getEmail());
    }

    @Test
    void fromUserDtoShouldMapUserDtoToUser() {
        UserDto userDto = new UserDto(1L, "User1", "user1@example.com");

        User result = UserMapper.fromUserDto(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("User1", result.getName());
        assertEquals("user1@example.com", result.getEmail());
    }

    @Test
    void fromUserDtoShouldHandleNullFields() {
        UserDto userDto = new UserDto(1L, null, null);

        User result = UserMapper.fromUserDto(userDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getName());
        assertNull(result.getEmail());
    }

    @Test
    void toUserDtoAndBackShouldPreserveData() {
        User originalUser = new User(1L, "User1", "user1@example.com");

        UserDto userDto = UserMapper.toUserDto(originalUser);
        User mappedUser = UserMapper.fromUserDto(userDto);

        assertEquals(originalUser.getId(), mappedUser.getId());
        assertEquals(originalUser.getName(), mappedUser.getName());
        assertEquals(originalUser.getEmail(), mappedUser.getEmail());
    }
}
