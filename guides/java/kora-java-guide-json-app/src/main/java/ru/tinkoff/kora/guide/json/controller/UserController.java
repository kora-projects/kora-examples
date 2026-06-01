package ru.tinkoff.kora.guide.json.controller;

import java.util.List;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.json.dto.UserRequest;
import ru.tinkoff.kora.guide.json.dto.UserResponse;
import ru.tinkoff.kora.guide.json.dto.UserResult;
import ru.tinkoff.kora.guide.json.service.UserService;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.Path;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;
import ru.tinkoff.kora.json.common.annotation.Json;

@Component
@HttpController
public final class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    public UserResponse createUser(@Json UserRequest request) {
        return userService.createUser(request);
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users/{id}")
    @Json
    public UserResult getUser(@Path String id) {
        return userService.getUser(id);
    }
}
