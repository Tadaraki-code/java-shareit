package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.RequestBookingDto;

import java.util.Collection;

public interface BookingService {

    BookingDto createBooking(RequestBookingDto bookingDto, Long userId);

    BookingDto updateBookingStatus(Long bookingId, boolean approved, Long userId);

    BookingDto getBooking(Long bookingId, Long userId);

    Collection<BookingDto> getUserBookings(String state, Long userId);

    Collection<BookingDto> getOwnerBookings(String state, Long userId);

    void deleteBooking(Long bookingId);
}
