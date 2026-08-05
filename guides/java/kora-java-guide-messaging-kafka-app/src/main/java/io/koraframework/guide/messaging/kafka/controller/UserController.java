package io.koraframework.guide.messaging.kafka.controller;

import org.jspecify.annotations.Nullable;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import io.koraframework.common.annotation.Component;
import io.koraframework.guide.messaging.kafka.dto.UserAcceptedResponse;
import io.koraframework.guide.messaging.kafka.dto.UserRequest;
import io.koraframework.guide.messaging.kafka.dto.UserResponse;
import io.koraframework.guide.messaging.kafka.kafka.UserCreatedEvent;
import io.koraframework.guide.messaging.kafka.kafka.UserCreatedPublisher;
import io.koraframework.guide.messaging.kafka.service.UserService;
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
