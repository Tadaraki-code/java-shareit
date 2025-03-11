package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.RequestDto;

@Service
public class RequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public RequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestBody RequestDto requestDto,
                                                @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return post("", ownerId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return get("", userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOtherRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return get("/all", userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId, Long userId) {
        return get("/" + requestId, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteRequestById(@PathVariable("id") Long requestId, @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        delete("/" + requestId, ownerId);
    }
}
