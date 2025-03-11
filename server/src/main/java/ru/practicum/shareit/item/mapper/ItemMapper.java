package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForRequest;
import ru.practicum.shareit.item.dto.ItemDtoWhitBooking;
import ru.practicum.shareit.item.dto.ItemDtoWhitComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;


public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        Long itemRequestId;
        if (item.getRequest() != null) {
            itemRequestId = item.getRequest().getId();
        } else {
            itemRequestId = null;
        }

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                itemRequestId
        );
    }

    public static ItemDtoWhitBooking toItemDtoWhitBooking(Item item,
                                                          Booking lastBooking,
                                                          Booking nextBooking,
                                                          Collection<Comment> comments) {
        BookingDto lastBookingDto = lastBooking != null ? BookingMapper.toBookingDto(lastBooking) : null;
        BookingDto nextBookingDto = nextBooking != null ? BookingMapper.toBookingDto(nextBooking) : null;

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

    public static Item fromItemDto(ItemDto itemDto, User owner, ItemRequest request) {
        return new Item(itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                request);
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

    public static ItemDtoForRequest toItemDtoForRequest(Item item) {
        return new ItemDtoForRequest(
                item.getId(),
                item.getName(),
                item.getOwner().getId());

    }
}
