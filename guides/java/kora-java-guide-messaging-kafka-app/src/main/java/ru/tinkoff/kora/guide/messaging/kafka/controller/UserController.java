package ru.tinkoff.kora.guide.messaging.kafka.controller;

import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserAcceptedResponse;
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserRequest;
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserResponse;
import ru.tinkoff.kora.guide.messaging.kafka.kafka.UserCreatedEvent;
import ru.tinkoff.kora.guide.messaging.kafka.kafka.UserCreatedPublisher;
import ru.tinkoff.kora.guide.messaging.kafka.service.UserService;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.common.annotation.Query;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.common.header.HttpHeaders;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

@Component
@HttpController
public final class UserController {

    private final UserCreatedPublisher userCreatedPublisher;
    private final UserService userService;

    public UserController(UserCreatedPublisher userCreatedPublisher, UserService userService) {
        this.userCreatedPublisher = userCreatedPublisher;
        this.userService = userService;
    }

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    public HttpResponseEntity<UserAcceptedResponse> createUser(@Json UserRequest request) {
        var userId = UUID.randomUUID().toString();
        var event = new UserCreatedEvent(userId, request.name(), request.email(), LocalDateTime.now());
        this.userCreatedPublisher.send(event);
        return HttpResponseEntity.of(202, HttpHeaders.of(), new UserAcceptedResponse(userId));
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    public UserResponse getUser(@Path String userId) {
        return this.userService.getUser(userId)
                .orElseThrow(() -> HttpServerResponseException.of(404, "User not found"));
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    public List<UserResponse> getUsers(
            @Nullable @Query("page") Integer page,
            @Nullable @Query("size") Integer size,
            @Nullable @Query("sort") String sort) {
        int effectivePage = page == null ? 0 : page;
        int effectiveSize = size == null ? 10 : size;
        String effectiveSort = sort == null ? "name" : sort;
        return this.userService.getUsers(effectivePage, effectiveSize, effectiveSort);
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/users/{userId}")
    @Json
    public HttpResponseEntity<UserResponse> updateUser(@Path String userId, @Json UserRequest request) {
        var updated = this.userService.updateUser(userId, request);
        return HttpResponseEntity.of(200, HttpHeaders.of("X-Updated-At", Instant.now().toString()), updated);
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    public HttpServerResponse deleteUser(@Path String userId) {
        this.userService.deleteUser(userId);
        return HttpServerResponse.of(204, HttpBody.empty());
    }
}
