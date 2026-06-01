package ru.tinkoff.kora.guide.grpcserver.advanced.service;

import java.util.List;
import java.util.Optional;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserRequest;
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserResponse;

@Component
public final class UserStreamingService {

    private final UserService userService;

    public UserStreamingService(UserService userService) {
        this.userService = userService;
    }

    public List<UserResponse> getAllUsers() {
        return userService.getUsers(0, Integer.MAX_VALUE, "name");
    }

    public List<UserResponse> createUsers(List<UserRequest> requests) {
        return requests.stream()
                .map(userService::createUser)
                .toList();
    }

    public Optional<UserResponse> tryUpdateUser(String id, UserRequest request) {
        try {
            return Optional.of(userService.updateUser(id, request));
        } catch (UserNotFoundException e) {
            return Optional.empty();
        }
    }
}
