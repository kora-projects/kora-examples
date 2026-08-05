package io.koraframework.example.grpc.client;

import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Root;
import io.koraframework.generated.grpc.UserServiceGrpc;

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
