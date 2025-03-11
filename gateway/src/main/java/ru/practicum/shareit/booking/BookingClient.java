package ru.practicum.shareit.booking;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createBooking(long userId, BookItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> updateBookingStatus(Long bookingId, boolean approved, Long userId) {
        Map<String, Object> parameters = Map.of(
                "approved", approved
        );
        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getBooking(Long bookingId, Long userId) {
        return get("/" + bookingId, userId);
    }


    public ResponseEntity<Object> getUserBookings(BookingState state, Long userId) {
        Map<String, Object> parameters = Map.of(
                "state", state.name()
        );
        System.out.println(parameters);
        System.out.println(state.name());
        return get("?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(BookingState state, Long userId) {
        Map<String, Object> parameters = Map.of(
                "state", state.name()
        );
        System.out.println(state.name());
        System.out.println(parameters);
        return get("/owner?state={state}", userId, parameters);
    }

    public void deleteBooking(Long bookingId, Long userId) {
        delete("/" + bookingId, userId);
    }
}

