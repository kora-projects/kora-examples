package ru.tinkoff.kora.guide.grpcserver.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import java.util.ArrayList;
import java.util.Iterator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;

@KoraAppTest(Application.class)
class GrpcServerAdvancedAppTest {

    private static final int GRPC_PORT = 8093;
    private static final String API_KEY = "test-api-key";

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
        var userStub = newUserBlockingStub();

        var created = createUser(userStub, "Alice", "alice@example.com");

        assertEquals("Alice", created.getName());
        assertEquals("alice@example.com", created.getEmail());
    }

    @Test
    void getsUserOverGrpc() {
        var userStub = newUserBlockingStub();

        var created = createUser(userStub, "Alice", "alice@example.com");

        var loaded = userStub.getUser(GetUserRequest.newBuilder()
                .setUserId(created.getId())
                .build());

        assertEquals("Alice", loaded.getName());
        assertEquals(created.getId(), loaded.getId());
    }

    @Test
    void listsUsersOverGrpc() {
        var userStub = newUserBlockingStub();

        createUser(userStub, "Alice", "alice@example.com");
        createUser(userStub, "Bob", "bob@example.com");

        var listedUnary = userStub.getUsers(GetUsersRequest.newBuilder()
                .setPage(0)
                .setSize(10)
                .setSort("name")
                .build());

        assertEquals(2, listedUnary.getUsersCount());
    }

    @Test
    void updatesUserOverGrpc() {
        var userStub = newUserBlockingStub();

        var alice = createUser(userStub, "Alice", "alice@example.com");

        var updated = userStub.updateUser(UpdateUserRequestUnary.newBuilder()
                .setUserId(alice.getId())
                .setName("Alice Updated")
                .setEmail("alice.updated@example.com")
                .build());

        assertEquals("Alice Updated", updated.getName());
        assertEquals("alice.updated@example.com", updated.getEmail());
    }

    @Test
    void deletesUserOverGrpc() {
        var userStub = newUserBlockingStub();

        var bob = createUser(userStub, "Bob", "bob@example.com");

        userStub.deleteUser(DeleteUserRequest.newBuilder()
                .setUserId(bob.getId())
                .build());
    }

    @Test
    void streamsAllUsersOverGrpc() {
        var userStub = newUserBlockingStub();
        var streamingStub = newAuthorizedStreamingBlockingStub();

        var alice = createUser(userStub, "Alice", "alice@example.com");
        var bob = createUser(userStub, "Bob", "bob@example.com");

        Iterator<UserResponse> responseIterator = streamingStub.getAllUsers(Empty.getDefaultInstance());
        var users = new ArrayList<UserResponse>();
        responseIterator.forEachRemaining(users::add);

        assertEquals(2, users.size());
        assertEquals(alice.getId(), users.stream().filter(u -> u.getName().equals("Alice")).findFirst().orElseThrow().getId());
        assertEquals(bob.getId(), users.stream().filter(u -> u.getName().equals("Bob")).findFirst().orElseThrow().getId());
    }

    @Test
    void rejectsStreamingCallsWithoutApiKey() {
        var streamingStub = newStreamingBlockingStub();

        var error = assertThrows(StatusRuntimeException.class,
                () -> streamingStub.getAllUsers(Empty.getDefaultInstance()).hasNext());

        assertEquals(Status.Code.UNAUTHENTICATED, error.getStatus().getCode());
    }

    private UserServiceGrpc.UserServiceBlockingStub newUserBlockingStub() {
        return UserServiceGrpc.newBlockingStub(channel);
    }

    private UserStreamingServiceGrpc.UserStreamingServiceBlockingStub newStreamingBlockingStub() {
        return UserStreamingServiceGrpc.newBlockingStub(channel);
    }

    private UserStreamingServiceGrpc.UserStreamingServiceBlockingStub newAuthorizedStreamingBlockingStub() {
        var metadata = new Metadata();
        metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), API_KEY);
        return newStreamingBlockingStub().withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
    }

    private UserResponse createUser(UserServiceGrpc.UserServiceBlockingStub stub, String name, String email) {
        return stub.createUser(CreateUserRequest.newBuilder()
                .setName(name)
                .setEmail(email)
                .build());
    }
}
