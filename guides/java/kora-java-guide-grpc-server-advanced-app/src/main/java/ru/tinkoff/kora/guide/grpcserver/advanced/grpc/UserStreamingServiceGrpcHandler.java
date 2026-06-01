package ru.tinkoff.kora.guide.grpcserver.advanced.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUsersResponse;
import ru.tinkoff.kora.guide.grpcserver.advanced.UpdateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse;
import ru.tinkoff.kora.guide.grpcserver.advanced.UserStreamingServiceGrpc;
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserRequest;
import ru.tinkoff.kora.guide.grpcserver.advanced.service.UserStreamingService;

@Component
public final class UserStreamingServiceGrpcHandler extends UserStreamingServiceGrpc.UserStreamingServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserStreamingServiceGrpcHandler.class);

    private final UserStreamingService userStreamingService;

    public UserStreamingServiceGrpcHandler(UserStreamingService userStreamingService) {
        this.userStreamingService = userStreamingService;
    }

    @Override
    public void getAllUsers(Empty request, StreamObserver<UserResponse> responseObserver) {
        try {
            for (var user : userStreamingService.getAllUsers()) {
                responseObserver.onNext(toGrpcUser(user));
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Failed to stream users").withCause(e).asRuntimeException());
        }
    }

    @Override
    public StreamObserver<CreateUserRequest> createUsers(StreamObserver<CreateUsersResponse> responseObserver) {
        return new StreamObserver<>() {
            private final List<UserRequest> requests = new ArrayList<>();

            @Override
            public void onNext(CreateUserRequest value) {
                requests.add(new UserRequest(value.getName(), value.getEmail()));
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Client streaming failed", t);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                try {
                    var createdUsers = userStreamingService.createUsers(requests);
                    responseObserver.onNext(CreateUsersResponse.newBuilder()
                            .setCreatedCount(createdUsers.size())
                            .addAllUserIds(createdUsers.stream().map(ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserResponse::id).toList())
                            .build());
                    responseObserver.onCompleted();
                } catch (Exception e) {
                    responseObserver.onError(Status.INTERNAL.withDescription("Failed to create users").withCause(e).asRuntimeException());
                }
            }
        };
    }

    @Override
    public StreamObserver<UpdateUserRequest> updateUsers(StreamObserver<UserResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(UpdateUserRequest value) {
                try {
                    var user = userStreamingService.tryUpdateUser(value.getUserId(), new UserRequest(value.getName(), value.getEmail()))
                            .orElseThrow(() -> Status.NOT_FOUND.withDescription("User not found: " + value.getUserId()).asRuntimeException());
                    responseObserver.onNext(toGrpcUser(user));
                } catch (StatusRuntimeException e) {
                    responseObserver.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.error("Bidirectional streaming failed", t);
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private UserResponse toGrpcUser(ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserResponse user) {
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
