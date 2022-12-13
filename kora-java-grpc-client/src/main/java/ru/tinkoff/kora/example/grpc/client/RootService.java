package ru.tinkoff.kora.example.grpc.client;

import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.annotation.Root;
import ru.tinkoff.kora.generated.grpc.UserServiceGrpc;

@Root
@Component
public final class RootService {

    private final UserServiceGrpc.UserServiceBlockingStub userService;

    public RootService(UserServiceGrpc.UserServiceBlockingStub userService) {
        this.userService = userService;
    }

    public UserServiceGrpc.UserServiceBlockingStub service() {
        return userService;
    }
}
