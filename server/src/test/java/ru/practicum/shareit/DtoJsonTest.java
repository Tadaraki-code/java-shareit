package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.dto.ItemDtoWhitComments;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItem;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class DtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> jsonBookingDto;

    @Autowired
    private JacksonTester<ItemDtoWhitComments> jsonItemDtoWhitComments;

    @Autowired
    private JacksonTester<ItemRequestDtoWithItem> jsonItemRequestDtoWithItem;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private BookingDto bookingDto;
    private ItemDtoWhitComments itemDtoWhitComments;
    private ItemRequestDtoWithItem itemRequestDtoWithItem;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule()
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER)));

        LocalDateTime fixedTime = LocalDateTime.parse("2025-03-11T12:00:00", DATE_TIME_FORMATTER);

        ItemDto itemDto = new ItemDto(1L, "Item", "Description", true, null);
        UserDto userDto = new UserDto(2L, "Booker", "booker@example.com");
        bookingDto = new BookingDto(1L, fixedTime, fixedTime.plusDays(1), itemDto, userDto, Status.APPROVED);

        CommentDto commentDto = new CommentDto(1L, "Great item!", "Booker", fixedTime);
        itemDtoWhitComments = new ItemDtoWhitComments(
                1L, "Item", "Description", true, bookingDto,
                null, List.of(commentDto)
        );

        ItemDtoForRequest itemDtoForRequest = new ItemDtoForRequest(1L, "Tool", 1L);
        itemRequestDtoWithItem = new ItemRequestDtoWithItem(
                1L, "Need a tool", fixedTime, List.of(itemDtoForRequest)
        );
    }

    @Test
    void bookingDtoSerializationAndDeserializationShouldWork() throws Exception {
        String jsonContent = jsonBookingDto.write(bookingDto).getJson();
        assertThat(jsonContent)
                .contains("\"id\":1")
                .contains("\"start\":\"2025-03-11T12:00:00\"")
                .contains("\"end\":\"2025-03-12T12:00:00\"")
                .contains("\"item\":{\"id\":1,\"name\":\"Item\",\"description\":\"Description\"," +
                        "\"available\":true,\"requestId\":null}")
                .contains("\"booker\":{\"id\":2,\"name\":\"Booker\",\"email\":\"booker@example.com\"}")
                .contains("\"status\":\"APPROVED\"");

        BookingDto deserialized = jsonBookingDto.parse(jsonContent).getObject();
        assertThat(deserialized.getId()).isEqualTo(bookingDto.getId());
        assertThat(deserialized.getStart()).isEqualTo(bookingDto.getStart());
        assertThat(deserialized.getEnd()).isEqualTo(bookingDto.getEnd());
        assertThat(deserialized.getItem()).usingRecursiveComparison().isEqualTo(bookingDto.getItem());
        assertThat(deserialized.getBooker()).usingRecursiveComparison().isEqualTo(bookingDto.getBooker());
        assertThat(deserialized.getStatus()).isEqualTo(bookingDto.getStatus());
    }

    @Test
    void itemDtoWhitCommentsSerializationAndDeserializationShouldWork() throws Exception {
        String jsonContent = jsonItemDtoWhitComments.write(itemDtoWhitComments).getJson();
        assertThat(jsonContent)
                .contains("\"id\":1")
                .contains("\"name\":\"Item\"")
                .contains("\"description\":\"Description\"")
                .contains("\"available\":true")
                .contains("\"lastBooking\":{\"id\":1,\"start\":\"2025-03-11T12:00:00\",\"end\":\"2025-03-12T12:00:00\"")
                .contains("\"nextBooking\":null")
                .contains("\"comments\":[{\"id\":1,\"text\":\"Great item!\",\"authorName\":\"Booker\"," +
                        "\"created\":\"2025-03-11T12:00:00\"}]");

        ItemDtoWhitComments deserialized = jsonItemDtoWhitComments.parse(jsonContent).getObject();
        assertThat(deserialized.getId()).isEqualTo(itemDtoWhitComments.getId());
        assertThat(deserialized.getName()).isEqualTo(itemDtoWhitComments.getName());
        assertThat(deserialized.getDescription()).isEqualTo(itemDtoWhitComments.getDescription());
        assertThat(deserialized.getAvailable()).isEqualTo(itemDtoWhitComments.getAvailable());
        assertThat(deserialized.getLastBooking()).usingRecursiveComparison().isEqualTo(itemDtoWhitComments
                .getLastBooking());
        assertThat(deserialized.getNextBooking()).isNull();
        assertThat(deserialized.getComments()).hasSize(1);
        assertThat(deserialized.getComments().iterator().next().getText()).isEqualTo("Great item!");
        assertThat(deserialized.getComments().iterator().next().getAuthorName()).isEqualTo("Booker");
        assertThat(deserialized.getComments().iterator().next().getCreated()).isEqualTo(itemDtoWhitComments
                .getComments().iterator().next().getCreated());
    }

    @Test
    void itemRequestDtoWithItemSerializationAndDeserializationShouldWork() throws Exception {
        String jsonContent = jsonItemRequestDtoWithItem.write(itemRequestDtoWithItem).getJson();
        assertThat(jsonContent)
                .contains("\"id\":1")
                .contains("\"description\":\"Need a tool\"")
                .contains("\"created\":\"2025-03-11T12:00:00\"")
                .contains("\"items\":[{\"id\":1,\"name\":\"Tool\",\"ownerId\":1}]");

        ItemRequestDtoWithItem deserialized = jsonItemRequestDtoWithItem.parse(jsonContent).getObject();
        assertThat(deserialized.getId()).isEqualTo(itemRequestDtoWithItem.getId());
        assertThat(deserialized.getDescription()).isEqualTo(itemRequestDtoWithItem.getDescription());
        assertThat(deserialized.getCreated()).isEqualTo(itemRequestDtoWithItem.getCreated());
        assertThat(deserialized.getItems()).hasSize(1);
        assertThat(deserialized.getItems().getFirst().getId()).isEqualTo(itemRequestDtoWithItem.getItems().getFirst().getId());
        assertThat(deserialized.getItems().getFirst().getName()).isEqualTo(itemRequestDtoWithItem
                .getItems().getFirst().getName());
        assertThat(deserialized.getItems().getFirst().getOwnerId()).isEqualTo(itemRequestDtoWithItem
                .getItems().getFirst().getOwnerId());
    }
}
