package ru.tinkoff.kora.guide.validation.controller;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.validation.dto.UserRequest;
import ru.tinkoff.kora.guide.validation.dto.UserResponse;
import ru.tinkoff.kora.guide.validation.service.UserService;
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
import ru.tinkoff.kora.validation.common.annotation.NotBlank;
import ru.tinkoff.kora.validation.common.annotation.Pattern;
import ru.tinkoff.kora.validation.common.annotation.Range;
import ru.tinkoff.kora.validation.common.annotation.Valid;
import ru.tinkoff.kora.validation.common.annotation.Validate;

@Component
@HttpController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    @Validate
    public UserResponse getUser(@Path @NotBlank @Pattern("^\\d+$") String userId) {
        return userService.getUser(userId)
            .orElseThrow(() -> HttpServerResponseException.of(404, "User not found"));
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    @Validate
    public List<UserResponse> getUsers(
        @Nullable @Range(from = 0, to = 1_000) @Query("page") Integer page,
        @Nullable @Range(from = 1, to = 100) @Query("size") Integer size,
        @Nullable @Pattern("^(?i)(name|email|createdat)$") @Query("sort") String sort) {
        int pageNum = page == null ? 0 : page;
        int pageSize = size == null ? 10 : size;
        String sortBy = sort == null ? "name" : sort;
        return userService.getUsers(pageNum, pageSize, sortBy);
    }

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    @Validate
    public HttpResponseEntity<UserResponse> createUser(@Valid @Json UserRequest request) {
        UserResponse user = userService.createUser(request);
        return HttpResponseEntity.of(201, HttpHeaders.of(), user);
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/users/{userId}")
    @Json
    @Validate
    public HttpResponseEntity<UserResponse> updateUser(
        @Path @NotBlank @Pattern("^\\d+$") String userId,
        @Valid @Json UserRequest request) {
        UserResponse updated = userService.updateUser(userId, request);
        return HttpResponseEntity.of(200, HttpHeaders.of("X-Updated-At", Instant.now().toString()), updated);
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    @Validate
    public HttpServerResponse deleteUser(@Path @NotBlank @Pattern("^\\d+$") String userId) {
        userService.deleteUser(userId);
        return HttpServerResponse.of(204, HttpBody.empty());
    }
}
