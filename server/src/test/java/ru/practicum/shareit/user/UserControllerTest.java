package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.TestErrorHandler;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(TestErrorHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UserDto userDto = new UserDto(1L, "User1", "user1@example.com");

    @Test
    void findAllShouldReturnListOfUsers() throws Exception {
        List<UserDto> users = List.of(userDto);
        when(userService.getAllUser()).thenReturn(users);

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"));

        verify(userService, times(1)).getAllUser();
    }

    @Test
    void findAllShouldReturnEmptyListWhenNoUsers() throws Exception {
        when(userService.getAllUser()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userService, times(1)).getAllUser();
    }

    @Test
    void findUserShouldReturnUserDtoWhenUserExists() throws Exception {
        when(userService.getUser(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("User1"))
                .andExpect(jsonPath("$.email").value("user1@example.com"));

        verify(userService, times(1)).getUser(1L);
    }

    @Test
    void findUserShouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUser(1L)).thenThrow(new NotFoundException("Пользователь не найден."));

        mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден."));

        verify(userService, times(1)).getUser(1L);
    }

    @Test
    void createShouldReturnCreatedUserDto() throws Exception {
        UserDto inputDto = new UserDto(null, "User2", "user2@example.com");
        UserDto createdDto = new UserDto(2L, "User2", "user2@example.com");
        when(userService.createUser(any(UserDto.class))).thenReturn(createdDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("User2"))
                .andExpect(jsonPath("$.email").value("user2@example.com"));

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void createShouldReturn409WhenEmailExists() throws Exception {
        UserDto inputDto = new UserDto(null, "User2", "user1@example.com");
        when(userService.createUser(any(UserDto.class))).thenThrow(new AlreadyExistException("Пользователь с таким email уже существует."));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Пользователь с таким email уже существует."));

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void updateShouldReturnUpdatedUserDtoWhenNameUpdated() throws Exception {
        Map<String, String> update = new HashMap<>();
        update.put("name", "Updated User");
        UserDto updatedDto = new UserDto(1L, "Updated User", "user1@example.com");
        when(userService.updateUser(update, 1L)).thenReturn(updatedDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated User"))
                .andExpect(jsonPath("$.email").value("user1@example.com"));

        verify(userService, times(1)).updateUser(update, 1L);
    }

    @Test
    void updateShouldReturn404WhenUserNotFound() throws Exception {
        Map<String, String> update = new HashMap<>();
        update.put("name", "Updated User");
        when(userService.updateUser(update, 1L)).thenThrow(new NotFoundException("Пользователь не найден."));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден."));

        verify(userService, times(1)).updateUser(update, 1L);
    }

    @Test
    void updateShouldReturn400WhenNameIsBlank() throws Exception {
        Map<String, String> update = new HashMap<>();
        update.put("name", " ");
        when(userService.updateUser(update, 1L)).thenThrow(new ValidationException("В запросе на обновление " +
                "имени была передана пустая строчка"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("В запросе на обновление " +
                        "имени была передана пустая строчка"));

        verify(userService, times(1)).updateUser(update, 1L);
    }

    @Test
    void updateShouldReturn409WhenEmailExists() throws Exception {
        Map<String, String> update = new HashMap<>();
        update.put("email", "user2@example.com");
        when(userService.updateUser(update, 1L)).thenThrow(new AlreadyExistException("Пользователь с email " +
                "user2@example.com уже " + "существует"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Пользователь с email " +
                        "user2@example.com уже существует"));

        verify(userService, times(1)).updateUser(update, 1L);
    }

    @Test
    void deleteUserShouldReturnOk() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1L);
    }
}
