package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWhitBooking;
import ru.practicum.shareit.item.dto.ItemDtoWhitComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;


public class ItemMapper {

    public static ItemDto toItemDto(Item item) {

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable()
        );
    }

    public static ItemDtoWhitBooking toItemDtoWhitBooking(Item item,
                                                          Optional<Booking> lastBooking,
                                                          Optional<Booking> nextBooking,
                                                          Collection<Comment> comments) {
        BookingDto lastBookingDto = lastBooking
                .map(BookingMapper::toBookingDto)
                .orElse(null);

        BookingDto nextBookingDto = nextBooking
                .map(BookingMapper::toBookingDto)
                .orElse(null);

        return new ItemDtoWhitBooking(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                lastBookingDto,
                nextBookingDto,
                comments.stream().map(CommentMapper::toCommentDto).toList()
        );
    }

    public static Item fromItemDto(ItemDto itemDto, User owner) {
        return new Item(itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                null);
    }

    public static ItemDtoWhitComments toItemDtoWhitComments(Item item,
                                                            Collection<Comment> comments) {
        return new ItemDtoWhitComments(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                null,
                null,
                comments.stream().map(CommentMapper::toCommentDto).toList()
        );
    }
}
