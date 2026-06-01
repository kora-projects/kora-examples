package ru.tinkoff.kora.guide.openapi.httpserver.controller;

import java.time.Instant;
import java.time.ZoneOffset;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.openapi.httpserver.user.api.UsersApiDelegate;
import ru.tinkoff.kora.guide.openapi.httpserver.user.api.UsersApiResponses;
import ru.tinkoff.kora.guide.openapi.httpserver.user.model.ErrorResponseTO;
import ru.tinkoff.kora.guide.openapi.httpserver.user.model.UserRequestTO;
import ru.tinkoff.kora.guide.openapi.httpserver.user.model.UserResponseTO;
import ru.tinkoff.kora.guide.openapi.httpserver.dto.UserRequest;
import ru.tinkoff.kora.guide.openapi.httpserver.dto.UserResponse;
import ru.tinkoff.kora.guide.openapi.httpserver.service.UserService;

@Component
public final class UserApiDelegateImpl implements UsersApiDelegate {

    private final UserService userService;

    public UserApiDelegateImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UsersApiResponses.CreateUserApiResponse createUser(UserRequestTO userRequest) {
        var created = this.userService.createUser(new UserRequest(userRequest.name(), userRequest.email()));
        return new UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse(this.toGenerated(created));
    }

    @Override
    public UsersApiResponses.DeleteUserApiResponse deleteUser(String userId) {
        if (this.userService.getUser(userId).isEmpty()) {
            return new UsersApiResponses.DeleteUserApiResponse.DeleteUser404ApiResponse(
                    this.notFound(userId)
            );
        }

        this.userService.deleteUser(userId);
        return new UsersApiResponses.DeleteUserApiResponse.DeleteUser204ApiResponse();
    }

    @Override
    public UsersApiResponses.GetUserApiResponse getUser(String userId) {
        return this.userService.getUser(userId)
                .<UsersApiResponses.GetUserApiResponse>map(user -> new UsersApiResponses.GetUserApiResponse.GetUser200ApiResponse(this.toGenerated(user)))
                .orElseGet(() -> new UsersApiResponses.GetUserApiResponse.GetUser404ApiResponse(
                        this.notFound(userId)
                ));
    }

    @Override
    public UsersApiResponses.GetUsersApiResponse getUsers(Integer page, Integer size, String sort) {
        int effectivePage = page == null ? 0 : page;
        int effectiveSize = size == null ? 10 : size;
        String effectiveSort = sort == null ? "name" : sort;
        var users = this.userService.getUsers(effectivePage, effectiveSize, effectiveSort).stream()
                .map(this::toGenerated)
                .toList();
        return new UsersApiResponses.GetUsersApiResponse.GetUsers200ApiResponse(users);
    }

    @Override
    public UsersApiResponses.UpdateUserApiResponse updateUser(String userId, UserRequestTO userRequest) {
        if (this.userService.getUser(userId).isEmpty()) {
            return new UsersApiResponses.UpdateUserApiResponse.UpdateUser404ApiResponse(
                    this.notFound(userId)
            );
        }

        var updated = this.userService.updateUser(userId, new UserRequest(userRequest.name(), userRequest.email()));
        return new UsersApiResponses.UpdateUserApiResponse.UpdateUser200ApiResponse(
                this.toGenerated(updated),
                Instant.now().toString()
        );
    }

    private UserResponseTO toGenerated(UserResponse user) {
        return new UserResponseTO(
                user.id(),
                user.name(),
                user.email(),
                user.createdAt().atOffset(ZoneOffset.UTC)
        );
    }

    private ErrorResponseTO notFound(String userId) {
        return new ErrorResponseTO("User with id '" + userId + "' was not found");
    }
}


