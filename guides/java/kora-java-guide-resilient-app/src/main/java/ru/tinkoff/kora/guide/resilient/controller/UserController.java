package ru.tinkoff.kora.guide.resilient.controller;

import jakarta.annotation.Nullable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.resilient.dto.UserRequest;
import ru.tinkoff.kora.guide.resilient.dto.UserResponse;
import ru.tinkoff.kora.guide.resilient.service.UserService;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.*;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.common.header.HttpHeaders;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

import java.time.Instant;
import java.util.List;

@Component
@HttpController
public final class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    public UserResponse getUser(@Path String userId) {
        return userService.getUser(userId)
                .orElseThrow(() -> HttpServerResponseException.of(404, "User not found"));
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    public List<UserResponse> getUsers(
            @Nullable @Query("page") Integer page,
            @Nullable @Query("size") Integer size,
            @Nullable @Query("sort") String sort) {
        int pageNum = page == null ? 0 : page;
        int pageSize = size == null ? 10 : size;
        String sortBy = sort == null ? "name" : sort;
        return userService.getUsers(pageNum, pageSize, sortBy);
    }

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    public HttpResponseEntity<UserResponse> createUser(
            @Json UserRequest request,
            @Nullable @Header("X-Request-ID") String requestId,
            @Nullable @Header("User-Agent") String userAgent,
            @Nullable @Cookie("sessionId") String sessionId) {
        UserResponse user = userService.createUser(request);
        return HttpResponseEntity.of(201, HttpHeaders.of(), user);
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/users/{userId}")
    @Json
    public HttpResponseEntity<UserResponse> updateUser(@Path String userId, @Json UserRequest request) {
        UserResponse updated = userService.updateUser(userId, request);
        return HttpResponseEntity.of(200, HttpHeaders.of("X-Updated-At", Instant.now().toString()), updated);
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    public HttpServerResponse deleteUser(@Path String userId) {
        userService.deleteUser(userId);
        return HttpServerResponse.of(204, HttpBody.empty());
    }
}


