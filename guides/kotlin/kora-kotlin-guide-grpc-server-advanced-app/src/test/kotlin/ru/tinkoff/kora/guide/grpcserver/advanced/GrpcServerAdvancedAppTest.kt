package ru.tinkoff.kora.guide.grpcserver.advanced

import com.google.protobuf.Empty
import io.grpc.*
import io.grpc.stub.MetadataUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest

@KoraAppTest(Application::class)
class GrpcServerAdvancedAppTest {
    private lateinit var channel: ManagedChannel

    @BeforeEach
    fun setUp() {
        channel = ManagedChannelBuilder.forAddress("localhost", GRPC_PORT).usePlaintext().build()
    }

    @AfterEach
    fun tearDown() {
        channel.shutdownNow()
    }

    @Test
    fun createsUserOverGrpc() {
        val created = createUser(newUserBlockingStub(), "Alice", "alice@example.com")

        assertEquals("Alice", created.name)
        assertEquals("alice@example.com", created.email)
    }

    @Test
    fun getsUserOverGrpc() {
        val userStub = newUserBlockingStub()
        val created = createUser(userStub, "Alice", "alice@example.com")

        val loaded = userStub.getUser(GetUserRequest.newBuilder()
            .setUserId(created.id)
            .build())

        assertEquals("Alice", loaded.name)
        assertEquals(created.id, loaded.id)
    }

    @Test
    fun listsUsersOverGrpc() {
        val userStub = newUserBlockingStub()

        createUser(userStub, "Alice", "alice@example.com")
        createUser(userStub, "Bob", "bob@example.com")

        val listedUnary = userStub.getUsers(GetUsersRequest.newBuilder()
            .setPage(0)
            .setSize(10)
            .setSort("name")
            .build())

        assertEquals(2, listedUnary.usersCount)
    }

    @Test
    fun updatesUserOverGrpc() {
        val userStub = newUserBlockingStub()
        val alice = createUser(userStub, "Alice", "alice@example.com")

        val updated = userStub.updateUser(UpdateUserRequestUnary.newBuilder()
            .setUserId(alice.id)
            .setName("Alice Updated")
            .setEmail("alice.updated@example.com")
            .build())

        assertEquals("Alice Updated", updated.name)
        assertEquals("alice.updated@example.com", updated.email)
    }

    @Test
    fun deletesUserOverGrpc() {
        val userStub = newUserBlockingStub()
        val bob = createUser(userStub, "Bob", "bob@example.com")

        userStub.deleteUser(DeleteUserRequest.newBuilder()
            .setUserId(bob.id)
            .build())
    }

    @Test
    fun streamsAllUsersOverGrpc() {
        val userStub = newUserBlockingStub()
        val streamingStub = newAuthorizedStreamingBlockingStub()
        val alice = createUser(userStub, "Alice", "alice@example.com")
        val bob = createUser(userStub, "Bob", "bob@example.com")

        val users = streamingStub.getAllUsers(Empty.getDefaultInstance()).asSequence().toList()

        assertEquals(alice.id, users.first { it.name == "Alice" }.id)
        assertEquals(bob.id, users.first { it.name == "Bob" }.id)
    }

    @Test
    fun rejectsStreamingCallsWithoutApiKey() {
        val error = assertThrows(StatusRuntimeException::class.java) {
            newStreamingBlockingStub().getAllUsers(Empty.getDefaultInstance()).hasNext()
        }

        assertEquals(Status.Code.UNAUTHENTICATED, error.status.code)
    }

    private fun newUserBlockingStub(): UserServiceGrpc.UserServiceBlockingStub =
        UserServiceGrpc.newBlockingStub(channel)

    private fun newStreamingBlockingStub(): UserStreamingServiceGrpc.UserStreamingServiceBlockingStub =
        UserStreamingServiceGrpc.newBlockingStub(channel)

    private fun newAuthorizedStreamingBlockingStub(): UserStreamingServiceGrpc.UserStreamingServiceBlockingStub {
        val metadata = Metadata()
        metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), API_KEY)
        return newStreamingBlockingStub().withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
    }

    private fun createUser(stub: UserServiceGrpc.UserServiceBlockingStub, name: String, email: String): UserResponse =
        stub.createUser(CreateUserRequest.newBuilder().setName(name).setEmail(email).build())

    companion object {
        private const val GRPC_PORT = 8093
        private const val API_KEY = "test-api-key"
    }
}
