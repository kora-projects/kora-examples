package ru.tinkoff.kora.guide.grpcclient.advanced.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserRequest;
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserResponse;
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserUpdateRequest;
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUsersResponse;
import ru.tinkoff.kora.guide.grpcserver.advanced.UpdateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.advanced.UserStreamingServiceGrpc;

@Component
public final class UserStreamingClientService {

    private final UserStreamingServiceGrpc.UserStreamingServiceBlockingStub blockingStub;
    private final UserStreamingServiceGrpc.UserStreamingServiceStub asyncStub;

    public UserStreamingClientService(
            UserStreamingServiceGrpc.UserStreamingServiceBlockingStub blockingStub,
            UserStreamingServiceGrpc.UserStreamingServiceStub asyncStub) {
        this.blockingStub = blockingStub;
        this.asyncStub = asyncStub;
    }

    public CreateUsersResult createUsers(List<UserRequest> requests) {
        var future = new CompletableFuture<CreateUsersResult>();
        var responseObserver = new StreamObserver<CreateUsersResponse>() {
            @Override
            public void onNext(CreateUsersResponse value) {
                future.complete(new CreateUsersResult(value.getCreatedCount(), List.copyOf(value.getUserIdsList())));
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
            }
        };

        var requestObserver = this.asyncStub.createUsers(responseObserver);
        try {
            for (var request : requests) {
                requestObserver.onNext(CreateUserRequest.newBuilder()
                        .setName(request.name())
                        .setEmail(request.email())
                        .build());
            }
            requestObserver.onCompleted();
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            requestObserver.onError(e);
            throw new RuntimeException("Failed to create users over gRPC streaming", e);
        }
    }

    public List<UserResponse> getAllUsers() {
        var users = new ArrayList<UserResponse>();
        var iterator = this.blockingStub.getAllUsers(Empty.getDefaultInstance());
        iterator.forEachRemaining(user -> users.add(toDto(user)));
        return users;
    }

    public List<UserResponse> updateUsers(List<UserUpdateRequest> updates) {
        var future = new CompletableFuture<List<UserResponse>>();
        var responses = new CopyOnWriteArrayList<UserResponse>();
        var responseObserver = new StreamObserver<ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse>() {
            @Override
            public void onNext(ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse value) {
                responses.add(toDto(value));
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                future.complete(List.copyOf(responses));
            }
        };

        var requestObserver = this.asyncStub.updateUsers(responseObserver);
        try {
            for (var update : updates) {
                requestObserver.onNext(UpdateUserRequest.newBuilder()
                        .setUserId(update.userId())
                        .setName(update.name())
                        .setEmail(update.email())
                        .build());
            }
            requestObserver.onCompleted();
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            requestObserver.onError(e);
            throw new RuntimeException("Failed to update users over gRPC streaming", e);
        }
    }

    private UserResponse toDto(ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse response) {
        return new UserResponse(
                response.getId(),
                response.getName(),
                response.getEmail(),
                LocalDateTime.ofEpochSecond(
                        response.getCreatedAt().getSeconds(),
                        response.getCreatedAt().getNanos(),
                        ZoneOffset.UTC));
    }

    public record CreateUsersResult(int createdCount, List<String> userIds) {}
}
