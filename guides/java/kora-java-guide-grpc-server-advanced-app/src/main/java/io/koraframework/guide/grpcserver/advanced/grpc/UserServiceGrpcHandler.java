package io.koraframework.guide.grpcserver.advanced.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.ZoneOffset;
import io.koraframework.common.annotation.Component;
import io.koraframework.guide.grpcserver.advanced.CreateUserRequest;
import io.koraframework.guide.grpcserver.advanced.DeleteUserRequest;
import io.koraframework.guide.grpcserver.advanced.GetUserRequest;
import io.koraframework.guide.grpcserver.advanced.GetUsersRequest;
import io.koraframework.guide.grpcserver.advanced.GetUsersResponse;
import io.koraframework.guide.grpcserver.advanced.UpdateUserRequestUnary;
import io.koraframework.guide.grpcserver.advanced.UserResponse;
import io.koraframework.guide.grpcserver.advanced.UserServiceGrpc;
import io.koraframework.guide.grpcserver.advanced.dto.UserRequest;
import io.koraframework.guide.grpcserver.advanced.service.UserNotFoundException;
import io.koraframework.guide.grpcserver.advanced.service.UserService;

@Component
public final class UserServiceGrpcHandler extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    public UserServiceGrpcHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            var user = userService.createUser(new UserRequest(request.getName(), request.getEmail()));
            responseObserver.onNext(toGrpcUser(user));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to create user").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            var user = userService.getUser(request.getUserId())
                    .orElseThrow(() -> Status.NOT_FOUND.withDescription("User not found: " + request.getUserId()).asRuntimeException());
            responseObserver.onNext(toGrpcUser(user));
            responseObserver.onCompleted();
        } catch (RuntimeException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getUsers(GetUsersRequest request, StreamObserver<GetUsersResponse> responseObserver) {
        try {
            int page = request.getPage();
            int size = request.getSize() == 0 ? 10 : request.getSize();
            String sort = request.getSort().isBlank() ? "name" : request.getSort();
            responseObserver.onNext(GetUsersResponse.newBuilder()
                    .addAllUsers(userService.getUsers(page, size, sort).stream().map(this::toGrpcUser).toList())
                    .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to get users").withCause(e).asRuntimeException());
        }
    }

    @Override
    public void updateUser(UpdateUserRequestUnary request, StreamObserver<UserResponse> responseObserver) {
        try {
            var user = userService.updateUser(request.getUserId(), new UserRequest(request.getName(), request.getEmail()));
            responseObserver.onNext(toGrpcUser(user));
            responseObserver.onCompleted();
        } catch (UserNotFoundException e) {
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
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private UserResponse toGrpcUser(io.koraframework.guide.grpcserver.advanced.dto.UserResponse user) {
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
