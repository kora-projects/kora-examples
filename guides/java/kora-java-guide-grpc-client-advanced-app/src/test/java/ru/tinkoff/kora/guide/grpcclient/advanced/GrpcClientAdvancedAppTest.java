package ru.tinkoff.kora.guide.grpcclient.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.ClientInterceptors;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.Contexts;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserRequest;
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserUpdateRequest;
import ru.tinkoff.kora.guide.grpcclient.advanced.grpc.LoggingInterceptor;
import ru.tinkoff.kora.guide.grpcclient.advanced.grpc.UserStreamingAuthConfig;
import ru.tinkoff.kora.guide.grpcclient.advanced.grpc.UserStreamingAuthInterceptor;
import ru.tinkoff.kora.guide.grpcclient.advanced.service.UserStreamingClientService;
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUsersResponse;
import ru.tinkoff.kora.guide.grpcserver.advanced.UpdateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse;
import ru.tinkoff.kora.guide.grpcserver.advanced.UserStreamingServiceGrpc;

class GrpcClientAdvancedAppTest {

    private static final Metadata.Key<String> AUTHORIZATION_HEADER =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
    private static final Context.Key<String> AUTHORIZATION_CONTEXT_KEY = Context.key("authorization");

    private Server server;
    private ManagedChannel channel;
    private UserStreamingClientService userStreamingClientService;

    @BeforeEach
    void setUp() throws IOException {
        var service = ServerInterceptors.intercept(new FakeUserStreamingService(), new FakeAuthServerInterceptor());
        var serverName = InProcessServerBuilder.generateName();
        this.server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(service)
                .build()
                .start();
        this.channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        var authConfig = (UserStreamingAuthConfig) () -> "test-api-key";
        var interceptedChannel = ClientInterceptors.intercept(
                this.channel,
                new UserStreamingAuthInterceptor(authConfig),
                new LoggingInterceptor());
        this.userStreamingClientService = new UserStreamingClientService(
                UserStreamingServiceGrpc.newBlockingStub(interceptedChannel),
                UserStreamingServiceGrpc.newStub(interceptedChannel));
    }

    @AfterEach
    void tearDown() {
        if (this.channel != null) {
            this.channel.shutdownNow();
        }
        if (this.server != null) {
            this.server.shutdownNow();
        }
    }

    @Test
    void createUsersReturnsCreatedIdsFromInProcessGrpcServer() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var result = this.userStreamingClientService.createUsers(List.of(
                new UserRequest("Alice " + unique, "alice-" + unique + "@example.com"),
                new UserRequest("Bob " + unique, "bob-" + unique + "@example.com")));

        assertEquals(2, result.createdCount());
        assertEquals(2, result.userIds().size());
    }

    @Test
    void getAllUsersReturnsStreamedUsersFromInProcessGrpcServer() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var created = this.userStreamingClientService.createUsers(List.of(
                new UserRequest("Stream Alice " + unique, "stream-alice-" + unique + "@example.com"),
                new UserRequest("Stream Bob " + unique, "stream-bob-" + unique + "@example.com")));

        var users = this.userStreamingClientService.getAllUsers();

        assertTrue(created.userIds().stream()
                .allMatch(id -> users.stream().anyMatch(user -> user.id().equals(id))));
    }

    @Test
    void updateUsersReturnsUpdatedUsersFromInProcessGrpcServer() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var created = this.userStreamingClientService.createUsers(List.of(
                new UserRequest("Original Alice " + unique, "original-alice-" + unique + "@example.com"),
                new UserRequest("Original Bob " + unique, "original-bob-" + unique + "@example.com")));

        var updated = this.userStreamingClientService.updateUsers(List.of(
                new UserUpdateRequest(created.userIds().get(0), "Updated Alice " + unique, "updated-alice-" + unique + "@example.com"),
                new UserUpdateRequest(created.userIds().get(1), "Updated Bob " + unique, "updated-bob-" + unique + "@example.com")));

        assertTrue(updated.stream().anyMatch(user -> ("Updated Alice " + unique).equals(user.name())));
        assertTrue(updated.stream().anyMatch(user -> ("Updated Bob " + unique).equals(user.name())));
    }

    @Test
    void rejectsStreamingCallsWithoutApiKeyAgainstInProcessGrpcServer() {
        var unauthenticatedService = new UserStreamingClientService(
                UserStreamingServiceGrpc.newBlockingStub(this.channel),
                UserStreamingServiceGrpc.newStub(this.channel));

        var exception = assertThrows(StatusRuntimeException.class, unauthenticatedService::getAllUsers);

        assertEquals(Status.Code.UNAUTHENTICATED, exception.getStatus().getCode());
    }

    private static final class FakeAuthServerInterceptor implements ServerInterceptor {

        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                ServerCall<ReqT, RespT> call,
                Metadata headers,
                ServerCallHandler<ReqT, RespT> next) {
            var authorization = headers.get(AUTHORIZATION_HEADER);
            if (!"test-api-key".equals(authorization)) {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid API key"), new Metadata());
                return new ServerCall.Listener<>() {};
            }

            var context = Context.current().withValue(AUTHORIZATION_CONTEXT_KEY, authorization);
            return Contexts.interceptCall(context, call, headers, next);
        }
    }

    private static final class FakeUserStreamingService extends UserStreamingServiceGrpc.UserStreamingServiceImplBase {

        private final List<StoredUser> users = new CopyOnWriteArrayList<>();

        @Override
        public StreamObserver<CreateUserRequest> createUsers(StreamObserver<CreateUsersResponse> responseObserver) {
            var createdIds = new CopyOnWriteArrayList<String>();
            return new StreamObserver<>() {
                @Override
                public void onNext(CreateUserRequest value) {
                    var user = new StoredUser(
                            UUID.randomUUID().toString(),
                            value.getName(),
                            value.getEmail(),
                            LocalDateTime.now(ZoneOffset.UTC));
                    users.add(user);
                    createdIds.add(user.id());
                }

                @Override
                public void onError(Throwable t) {
                    responseObserver.onError(t);
                }

                @Override
                public void onCompleted() {
                    responseObserver.onNext(CreateUsersResponse.newBuilder()
                            .setCreatedCount(createdIds.size())
                            .addAllUserIds(createdIds)
                            .build());
                    responseObserver.onCompleted();
                }
            };
        }

        @Override
        public void getAllUsers(Empty request, StreamObserver<UserResponse> responseObserver) {
            for (var user : this.users) {
                responseObserver.onNext(toGrpc(user));
            }
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<UpdateUserRequest> updateUsers(StreamObserver<UserResponse> responseObserver) {
            return new StreamObserver<>() {
                @Override
                public void onNext(UpdateUserRequest value) {
                    for (int i = 0; i < users.size(); i++) {
                        var existing = users.get(i);
                        if (existing.id().equals(value.getUserId())) {
                            var updated = new StoredUser(
                                    existing.id(),
                                    value.getName(),
                                    value.getEmail(),
                                    existing.createdAt());
                            users.set(i, updated);
                            responseObserver.onNext(toGrpc(updated));
                            return;
                        }
                    }
                    responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
                }

                @Override
                public void onError(Throwable t) {
                    responseObserver.onError(t);
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }

        private static UserResponse toGrpc(StoredUser user) {
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

    private record StoredUser(String id, String name, String email, LocalDateTime createdAt) {}
}
