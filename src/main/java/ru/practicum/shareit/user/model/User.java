package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private Long id;
    @NotEmpty(message = "Имя не может быть пустым!")
    private String name;
    @NotEmpty(message = "Поле Email не может быть пустым!")
    @Email(message = "Email имеет не надлежащий формат!")
    private String email;
}
