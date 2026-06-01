package ru.tinkoff.kora.guide.grpcclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.grpcclient.dto.UserRequest;
import ru.tinkoff.kora.guide.grpcclient.service.UserClientService;
import ru.tinkoff.kora.guide.grpcserver.CreateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.DeleteUserRequest;
import ru.tinkoff.kora.guide.grpcserver.GetUserRequest;
import ru.tinkoff.kora.guide.grpcserver.GetUsersRequest;
import ru.tinkoff.kora.guide.grpcserver.GetUsersResponse;
import ru.tinkoff.kora.guide.grpcserver.UpdateUserRequest;
import ru.tinkoff.kora.guide.grpcserver.UserResponse;
import ru.tinkoff.kora.guide.grpcserver.UserServiceGrpc;

class GrpcClientAppTest {

    private Server server;
    private ManagedChannel channel;
    private UserClientService userClientService;

    @BeforeEach
    void setUp() throws IOException {
        var service = new FakeUserService();
        var serverName = InProcessServerBuilder.generateName();
        this.server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(service)
                .build()
                .start();
        this.channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();
        this.userClientService = new UserClientService(UserServiceGrpc.newBlockingStub(this.channel));
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
    void createUserReturnsCreatedUserFromInProcessGrpcServer() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var created = this.userClientService.createUser(new UserRequest("Client User " + unique, "client-" + unique + "@example.com"));

        assertEquals("Client User " + unique, created.name());
        assertTrue(created.id() != null && !created.id().isBlank());
    }

    @Test
    void getUserReturnsUserFromInProcessGrpcServer() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var created = this.userClientService.createUser(new UserRequest("Lookup User " + unique, "lookup-" + unique + "@example.com"));

        var user = this.userClientService.getUser(created.id());

        assertEquals(created.id(), user.id());
        assertEquals("Lookup User " + unique, user.name());
    }

    @Test
    void getMissingUserThrowsAgainstInProcessGrpcServer() {
        var exception = assertThrows(StatusRuntimeException.class, () -> this.userClientService.getUser("missing-user"));
        assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
    }

    @Test
    void getUsersReturnsSortedUsersFromInProcessGrpcServer() {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        var userA = this.userClientService.createUser(new UserRequest("Alpha " + suffix, "a-" + suffix + "@example.com"));
        var userB = this.userClientService.createUser(new UserRequest("Bravo " + suffix, "b-" + suffix + "@example.com"));
        var userC = this.userClientService.createUser(new UserRequest("Charlie " + suffix, "c-" + suffix + "@example.com"));

        var users = this.userClientService.getUsers(0, 100, "name");
        var filtered = users.stream()
                .filter(user -> List.of(userA.id(), userB.id(), userC.id()).contains(user.id()))
                .toList();

        assertEquals(List.of("Alpha " + suffix, "Bravo " + suffix, "Charlie " + suffix),
                filtered.stream().map(ru.tinkoff.kora.guide.grpcclient.dto.UserResponse::name).toList());
    }

    @Test
    void updateUserReturnsUpdatedUserFromInProcessGrpcServer() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var created = this.userClientService.createUser(new UserRequest("Original User " + unique, "original-" + unique + "@example.com"));

        var updated = this.userClientService.updateUser(created.id(),
                new UserRequest("Updated User " + unique, "updated-" + unique + "@example.com"));

        assertEquals(created.id(), updated.id());
        assertEquals("Updated User " + unique, updated.name());
        assertEquals("updated-" + unique + "@example.com", updated.email());
    }

    @Test
    void deleteUserRemovesUserFromInProcessGrpcServer() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        var created = this.userClientService.createUser(new UserRequest("Delete User " + unique, "delete-" + unique + "@example.com"));

        this.userClientService.deleteUser(created.id());

        var exception = assertThrows(StatusRuntimeException.class, () -> this.userClientService.getUser(created.id()));
        assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());
    }

    private static final class FakeUserService extends UserServiceGrpc.UserServiceImplBase {

        private final Map<String, StoredUser> users = new LinkedHashMap<>();

        @Override
        public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
            var user = new StoredUser(
                    UUID.randomUUID().toString(),
                    request.getName(),
                    request.getEmail(),
                    LocalDateTime.now(ZoneOffset.UTC));
            this.users.put(user.id(), user);
            responseObserver.onNext(toGrpc(user));
            responseObserver.onCompleted();
        }

        @Override
        public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
            var user = this.users.get(request.getUserId());
            if (user == null) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
                return;
            }
            responseObserver.onNext(toGrpc(user));
            responseObserver.onCompleted();
        }

        @Override
        public void getUsers(GetUsersRequest request, StreamObserver<GetUsersResponse> responseObserver) {
            var users = new ArrayList<>(this.users.values());
            if ("name".equals(request.getSort())) {
                users.sort(Comparator.comparing(StoredUser::name));
            }

            int fromIndex = Math.min(request.getPage() * request.getSize(), users.size());
            int toIndex = Math.min(fromIndex + request.getSize(), users.size());
            var page = users.subList(fromIndex, toIndex).stream()
                    .map(FakeUserService::toGrpc)
                    .toList();

            responseObserver.onNext(GetUsersResponse.newBuilder()
                    .addAllUsers(page)
                    .build());
            responseObserver.onCompleted();
        }

        @Override
        public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
            var existing = this.users.get(request.getUserId());
            if (existing == null) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException());
                return;
            }

            var updated = new StoredUser(existing.id(), request.getName(), request.getEmail(), existing.createdAt());
            this.users.put(updated.id(), updated);
            responseObserver.onNext(toGrpc(updated));
            responseObserver.onCompleted();
        }

        @Override
        public void deleteUser(DeleteUserRequest request, StreamObserver<Empty> responseObserver) {
            this.users.remove(request.getUserId());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
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
