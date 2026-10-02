package io.koraframework.guide.validation.controller;

import org.jspecify.annotations.Nullable;
import java.time.Instant;
import java.util.List;
import io.koraframework.common.annotation.Component;
import io.koraframework.guide.validation.dto.UserRequest;
import io.koraframework.guide.validation.dto.UserResponse;
import io.koraframework.guide.validation.service.UserService;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.common.annotation.Query;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.common.header.HttpHeaders;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.json.common.annotation.Json;
import io.koraframework.validation.common.annotation.NotBlank;
import io.koraframework.validation.common.annotation.Pattern;
import io.koraframework.validation.common.annotation.Range;
import io.koraframework.validation.common.annotation.Valid;
import io.koraframework.validation.common.annotation.Validate;

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
