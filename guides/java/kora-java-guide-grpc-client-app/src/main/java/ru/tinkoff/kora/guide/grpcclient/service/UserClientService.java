package ru.tinkoff.kora.guide.grpcclient.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.grpcclient.dto.UserRequest;
import ru.tinkoff.kora.guide.grpcclient.dto.UserResponse;
import ru.tinkoff.kora.guide.grpcserver.CreateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.DeleteUserRequest;
import ru.tinkoff.kora.guide.grpcserver.GetUserRequest;
import ru.tinkoff.kora.guide.grpcserver.GetUsersRequest;
import ru.tinkoff.kora.guide.grpcserver.UpdateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.UserServiceGrpc;

@Component
public final class UserClientService {

    private final UserServiceGrpc.UserServiceBlockingStub userService;

    public UserClientService(UserServiceGrpc.UserServiceBlockingStub userService) {
        this.userService = userService;
    }

    public UserResponse createUser(UserRequest request) {
        return toDto(this.userService.createUser(CreateUserRequest.newBuilder()
                .setName(request.name())
                .setEmail(request.email())
                .build()));
    }

    public UserResponse getUser(String userId) {
        return toDto(this.userService.getUser(GetUserRequest.newBuilder()
                .setUserId(userId)
                .build()));
    }

    public List<UserResponse> getUsers(int page, int size, String sort) {
        return this.userService.getUsers(GetUsersRequest.newBuilder()
                        .setPage(page)
                        .setSize(size)
                        .setSort(sort)
                        .build())
                .getUsersList().stream()
                .map(this::toDto)
                .toList();
    }

    public UserResponse updateUser(String userId, UserRequest request) {
        return toDto(this.userService.updateUser(UpdateUserRequest.newBuilder()
                .setUserId(userId)
                .setName(request.name())
                .setEmail(request.email())
                .build()));
    }

    public void deleteUser(String userId) {
        this.userService.deleteUser(DeleteUserRequest.newBuilder()
                .setUserId(userId)
                .build());
    }

    private UserResponse toDto(ru.tinkoff.kora.guide.grpcserver.UserResponse response) {
        return new UserResponse(
                response.getId(),
                response.getName(),
                response.getEmail(),
                LocalDateTime.ofEpochSecond(
                        response.getCreatedAt().getSeconds(),
                        response.getCreatedAt().getNanos(),
                        ZoneOffset.UTC));
    }
}
