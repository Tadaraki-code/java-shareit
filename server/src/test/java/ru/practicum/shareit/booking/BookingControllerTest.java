package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.RequestBookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2);

    private final Long userId = 1L;
    private final BookingDto bookingDto = new BookingDto(
            1L,
            start,
            end,
            new ItemDto(1L, "Item1", "Description", true, null),
            new UserDto(1L, "User1", "user1@example.com"),
            Status.WAITING
    );

    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void createBookingShouldReturnBookingDtoWhenValid() throws Exception {
        RequestBookingDto requestBookingDto = new RequestBookingDto(1L, start, end);
        when(bookingService.createBooking(any(RequestBookingDto.class), eq(userId))).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId().intValue())))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(formatter))))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));

        verify(bookingService, times(1)).createBooking(any(RequestBookingDto.class), eq(userId));
    }

    @Test
    void getBookingShouldReturnBookingDtoWhenValid() throws Exception {
        Long bookingId = 1L;
        when(bookingService.getBooking(bookingId, userId)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId().intValue())))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().format(formatter))))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId().intValue())))
                .andExpect(jsonPath("$.booker.id", is(bookingDto.getBooker().getId().intValue())))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));

        verify(bookingService, times(1)).getBooking(bookingId, userId);
    }

    @Test
    void getUserBookingsShouldReturnBookingListWhenValid() throws Exception {
        when(bookingService.getUserBookings("ALL", userId)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId().intValue())))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().format(formatter))))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd().format(formatter))));

        verify(bookingService, times(1)).getUserBookings("ALL", userId);
    }

    @Test
    void getOwnerBookingsShouldReturnBookingListWhenValid() throws Exception {
        when(bookingService.getOwnerBookings("ALL", userId)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId().intValue())))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().format(formatter))))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd().format(formatter))));

        verify(bookingService, times(1)).getOwnerBookings("ALL", userId);
    }

    @Test
    void deleteBookingShouldReturnNoContentWhenValid() throws Exception {
        Long bookingId = 1L;
        doNothing().when(bookingService).deleteBooking(bookingId, userId);

        mockMvc.perform(delete("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).deleteBooking(bookingId, userId);
    }

    @Test
    void updateBookingStatusShouldReturnUpdatedBookingDtoWhenValid() throws Exception {
        Long bookingId = 1L;
        BookingDto updatedBookingDto = new BookingDto(
                bookingId, bookingDto.getStart(), bookingDto.getEnd(), bookingDto.getItem(), bookingDto.getBooker(), Status.APPROVED
        );
        when(bookingService.updateBookingStatus(bookingId, true, userId)).thenReturn(updatedBookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedBookingDto.getId().intValue())))
                .andExpect(jsonPath("$.start", is(updatedBookingDto.getStart().format(formatter))))
                .andExpect(jsonPath("$.end", is(updatedBookingDto.getEnd().format(formatter))))
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(bookingService, times(1)).updateBookingStatus(bookingId, true, userId);
    }
}
