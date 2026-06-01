package ru.tinkoff.kora.guide.grpcserver.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.grpcserver.CreateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.DeleteUserRequest;
import ru.tinkoff.kora.guide.grpcserver.GetUserRequest;
import ru.tinkoff.kora.guide.grpcserver.GetUsersRequest;
import ru.tinkoff.kora.guide.grpcserver.GetUsersResponse;
import ru.tinkoff.kora.guide.grpcserver.UpdateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.UserResponse;
import ru.tinkoff.kora.guide.grpcserver.UserServiceGrpc;
import ru.tinkoff.kora.guide.grpcserver.dto.UserRequest;
import ru.tinkoff.kora.guide.grpcserver.service.UserNotFoundException;
import ru.tinkoff.kora.guide.grpcserver.service.UserService;

@Component
public final class UserServiceGrpcHandler extends UserServiceGrpc.UserServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceGrpcHandler.class);

    private final UserService userService;

    public UserServiceGrpcHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            logger.info("Creating user: name={}, email={}", request.getName(), request.getEmail());
            ru.tinkoff.kora.guide.grpcserver.dto.UserResponse user =
                    userService.createUser(new UserRequest(request.getName(), request.getEmail()));
            responseObserver.onNext(toGrpcUser(user));
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed to create user", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to create user")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            logger.info("Getting user: id={}", request.getUserId());
            ru.tinkoff.kora.guide.grpcserver.dto.UserResponse user = userService.getUser(request.getUserId())
                    .orElseThrow(() -> Status.NOT_FOUND
                            .withDescription("User not found: " + request.getUserId())
                            .asRuntimeException());
            responseObserver.onNext(toGrpcUser(user));
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            logger.error("Failed to get user", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getUsers(GetUsersRequest request, StreamObserver<GetUsersResponse> responseObserver) {
        try {
            int page = request.getPage();
            int size = request.getSize() == 0 ? 10 : request.getSize();
            String sort = request.getSort().isBlank() ? "name" : request.getSort();
            var response = GetUsersResponse.newBuilder()
                    .addAllUsers(userService.getUsers(page, size, sort).stream().map(this::toGrpcUser).toList())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Failed to get users", e);
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to get users").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            var updated = userService.updateUser(request.getUserId(), new UserRequest(request.getName(), request.getEmail()));
            responseObserver.onNext(toGrpcUser(updated));
            responseObserver.onCompleted();
        } catch (UserNotFoundException e) {
            logger.error("Failed to update user", e);
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<Empty> responseObserver) {
        try {
            userService.deleteUser(request.getUserId());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (UserNotFoundException e) {
            logger.error("Failed to delete user", e);
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private UserResponse toGrpcUser(ru.tinkoff.kora.guide.grpcserver.dto.UserResponse user) {
        return UserResponse.newBuilder()
                .setId(user.id())
                .setName(user.name())
                .setEmail(user.email())
                .setCreatedAt(Timestamp.newBuilder()
                        .setSeconds(user.createdAt().toEpochSecond(ZoneOffset.UTC))
                        .setNanos(user.createdAt().getNano())
                        .build())
                .build();
    }
}
