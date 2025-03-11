package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.validation.FutureWithInputLag;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
    @NotNull
    private Long itemId;
    @FutureWithInputLag(inputLag = 2)
    private LocalDateTime start;
    @FutureWithInputLag(inputLag = 2)
    private LocalDateTime end;
}
