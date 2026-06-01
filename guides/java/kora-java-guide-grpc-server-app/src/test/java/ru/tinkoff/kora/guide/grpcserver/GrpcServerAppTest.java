package ru.tinkoff.kora.guide.grpcserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;

@KoraAppTest(Application.class)
class GrpcServerAppTest {

    private static final int GRPC_PORT = 8091;

    private ManagedChannel channel;

    @BeforeEach
    void setUp() {
        this.channel = ManagedChannelBuilder.forAddress("localhost", GRPC_PORT)
                .usePlaintext()
                .build();
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    void createsUserOverGrpc() {
        var stub = newStub();

        var created = createUser(stub, "Alice", "alice@example.com");

        assertEquals("Alice", created.getName());
        assertEquals("alice@example.com", created.getEmail());
    }

    @Test
    void getsUserOverGrpc() {
        var stub = newStub();

        var created = createUser(stub, "Alice", "alice@example.com");

        var loaded = stub.getUser(GetUserRequest.newBuilder()
                .setUserId(created.getId())
                .build());

        assertEquals(created.getId(), loaded.getId());
        assertEquals("Alice", loaded.getName());
        assertEquals("alice@example.com", loaded.getEmail());
    }

    @Test
    void listsUsersOverGrpc() {
        var stub = newStub();

        createUser(stub, "Alice", "alice@example.com");
        createUser(stub, "Bob", "bob@example.com");

        var listed = stub.getUsers(GetUsersRequest.newBuilder()
                .setPage(0)
                .setSize(10)
                .setSort("name")
                .build());
        assertEquals(2, listed.getUsersCount());
    }

    @Test
    void updatesUserOverGrpc() {
        var stub = newStub();

        var created = createUser(stub, "Alice", "alice@example.com");

        var updated = stub.updateUser(UpdateUserRequest.newBuilder()
                .setUserId(created.getId())
                .setName("Alice Updated")
                .setEmail("alice.updated@example.com")
                .build());

        assertEquals("Alice Updated", updated.getName());
        assertEquals("alice.updated@example.com", updated.getEmail());
    }

    @Test
    void deletesUserOverGrpc() {
        var stub = newStub();

        var created = createUser(stub, "Alice", "alice@example.com");

        var deleted = stub.deleteUser(DeleteUserRequest.newBuilder()
                .setUserId(created.getId())
                .build());
        assertEquals(Empty.getDefaultInstance(), deleted);
    }

    @Test
    void returnsNotFoundForMissingUser() {
        var stub = newStub();

        var error = assertThrows(StatusRuntimeException.class, () -> stub.getUser(GetUserRequest.newBuilder()
                .setUserId("missing")
                .build()));

        assertEquals(Status.Code.NOT_FOUND, error.getStatus().getCode());
    }

    private UserServiceGrpc.UserServiceBlockingStub newStub() {
        return UserServiceGrpc.newBlockingStub(channel);
    }

    private UserResponse createUser(UserServiceGrpc.UserServiceBlockingStub stub, String name, String email) {
        return stub.createUser(CreateUserRequest.newBuilder()
                .setName(name)
                .setEmail(email)
                .build());
    }
}
