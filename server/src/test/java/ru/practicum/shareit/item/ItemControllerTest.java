package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWhitBooking;
import ru.practicum.shareit.item.dto.ItemDtoWhitComments;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemDto itemDto;
    private ItemDtoWhitBooking itemDtoWhitBooking;
    private ItemDtoWhitComments itemDtoWhitComments;
    private CommentDto commentDto;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule()
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER)));

        itemDto = new ItemDto(1L, "Item", "Description", true, null);
        itemDtoWhitBooking = new ItemDtoWhitBooking(1L, "Item", "Description",
                true, null, null, Collections.emptyList());
        itemDtoWhitComments = new ItemDtoWhitComments(1L, "Item", "Description",
                true, null, null, Collections.emptyList());
        commentDto = new CommentDto(1L, "Great item!", "Booker",
                LocalDateTime.parse("2025-03-11T12:00:00", DATE_TIME_FORMATTER));
    }

    @Test
    void createItemShouldReturnItemDto() throws Exception {
        when(itemService.createItem(any(ItemDto.class), eq(1L))).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Item")))
                .andExpect(jsonPath("$.description", is("Description")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.requestId").doesNotExist());

        verify(itemService, times(1)).createItem(any(ItemDto.class), eq(1L));
    }

    @Test
    void updateItemShouldReturnUpdatedItemDto() throws Exception {
        Map<String, String> update = Map.of("name", "Updated Item");
        ItemDto updatedItemDto = new ItemDto(1L, "Updated Item", "Description",
                true, null);
        when(itemService.updateItem(eq(update), eq(1L), eq(1L))).thenReturn(updatedItemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Item")))
                .andExpect(jsonPath("$.description", is("Description")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(itemService, times(1)).updateItem(eq(update), eq(1L), eq(1L));
    }

    @Test
    void getItemShouldReturnItemDtoWhitComments() throws Exception {
        when(itemService.getItem(1L)).thenReturn(itemDtoWhitComments);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Item")))
                .andExpect(jsonPath("$.description", is("Description")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.lastBooking").doesNotExist())
                .andExpect(jsonPath("$.nextBooking").doesNotExist())
                .andExpect(jsonPath("$.comments", hasSize(0)));

        verify(itemService, times(1)).getItem(1L);
    }

    @Test
    void getAllOwnerItemsShouldReturnListOfItemDtoWhitBooking() throws Exception {
        when(itemService.getAllOwnerItems(1L)).thenReturn(List.of(itemDtoWhitBooking));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Item")))
                .andExpect(jsonPath("$[0].description", is("Description")))
                .andExpect(jsonPath("$[0].available", is(true)))
                .andExpect(jsonPath("$[0].lastBooking").doesNotExist())
                .andExpect(jsonPath("$[0].nextBooking").doesNotExist())
                .andExpect(jsonPath("$[0].comments", hasSize(0)));

        verify(itemService, times(1)).getAllOwnerItems(1L);
    }

    @Test
    void searchItemsShouldReturnListOfItemDto() throws Exception {
        when(itemService.searchItems("item")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Item")))
                .andExpect(jsonPath("$[0].description", is("Description")))
                .andExpect(jsonPath("$[0].available", is(true)));

        verify(itemService, times(1)).searchItems("item");
    }

    @Test
    void deleteItemShouldCallService() throws Exception {
        doNothing().when(itemService).deleteItem(1L, 1L);

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemService, times(1)).deleteItem(1L, 1L);
    }

    @Test
    void createCommentShouldReturnCommentDto() throws Exception {
        when(itemService.createComment(any(CommentDto.class), eq(1L), eq(2L))).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Great item!")))
                .andExpect(jsonPath("$.authorName", is("Booker")))
                .andExpect(jsonPath("$.created", is("2025-03-11T12:00:00")));

        verify(itemService,
                times(1)).createComment(any(CommentDto.class), eq(1L), eq(2L));
    }
}
