package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItem;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private final Long userId = 1L;
    private ItemRequestDto requestDto;
    private ItemRequestDtoWithItem requestDtoWithItem;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule()
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER)));

        LocalDateTime fixedTime = LocalDateTime.parse("2025-03-11T12:00:00", DATE_TIME_FORMATTER);
        requestDto = new ItemRequestDto(1L, "Need a tool", fixedTime);
        requestDtoWithItem = new ItemRequestDtoWithItem(
                1L, "Need a tool", fixedTime, List.of(new ItemDtoForRequest(1L, "Tool", userId))
        );
    }

    @Test
    void createRequestShouldReturnItemRequestDtoWhenValid() throws Exception {
        ItemRequestDto inputDto = new ItemRequestDto(null, "Need a tool", null);
        when(itemRequestService.createItemRequest(any(ItemRequestDto.class), eq(userId))).thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId().intValue())))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(requestDto.getCreated().format(DATE_TIME_FORMATTER))));

        verify(itemRequestService, times(1))
                .createItemRequest(any(ItemRequestDto.class), eq(userId));
    }

    @Test
    void getOwnerRequestsShouldReturnRequestListWhenValid() throws Exception {
        when(itemRequestService.getAllOwnerRequest(userId)).thenReturn(List.of(requestDtoWithItem));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDtoWithItem.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(requestDtoWithItem.getDescription())))
                .andExpect(jsonPath("$[0].created", is(requestDtoWithItem.getCreated()
                        .format(DATE_TIME_FORMATTER))))
                .andExpect(jsonPath("$[0].items[0].id", is(requestDtoWithItem.getItems()
                        .getFirst().getId().intValue())));

        verify(itemRequestService, times(1)).getAllOwnerRequest(userId);
    }

    @Test
    void getAllOtherRequestsShouldReturnRequestListWhenValid() throws Exception {
        when(itemRequestService.getAllRequest(userId)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId().intValue())))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(requestDto.getCreated().format(DATE_TIME_FORMATTER))));

        verify(itemRequestService, times(1)).getAllRequest(userId);
    }

    @Test
    void getRequestByIdShouldReturnRequestDtoWithItemWhenValid() throws Exception {
        Long requestId = 1L;
        when(itemRequestService.getItemRequest(requestId)).thenReturn(requestDtoWithItem);

        mockMvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDtoWithItem.getId().intValue())))
                .andExpect(jsonPath("$.description", is(requestDtoWithItem.getDescription())))
                .andExpect(jsonPath("$.created", is(requestDtoWithItem.getCreated()
                        .format(DATE_TIME_FORMATTER))))
                .andExpect(jsonPath("$.items[0].id", is(requestDtoWithItem.getItems()
                        .getFirst().getId().intValue())));

        verify(itemRequestService, times(1)).getItemRequest(requestId);
    }

    @Test
    void deleteRequestByIdShouldReturnNoContentWhenValid() throws Exception {
        Long requestId = 1L;
        doNothing().when(itemRequestService).deleteItemRequest(userId, requestId);

        mockMvc.perform(delete("/requests/{id}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(itemRequestService, times(1)).deleteItemRequest(userId, requestId);
    }
}