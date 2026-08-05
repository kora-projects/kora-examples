package io.koraframework.guide.json.controller;

import java.util.List;
import io.koraframework.common.annotation.Component;
import io.koraframework.guide.json.dto.UserRequest;
import io.koraframework.guide.json.dto.UserResponse;
import io.koraframework.guide.json.dto.UserResult;
import io.koraframework.guide.json.service.UserService;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.Path;
import io.koraframework.http.server.common.annotation.HttpController;
import io.koraframework.json.common.annotation.Json;

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
