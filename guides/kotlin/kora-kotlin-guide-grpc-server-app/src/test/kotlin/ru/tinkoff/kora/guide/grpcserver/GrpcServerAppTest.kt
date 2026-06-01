package ru.tinkoff.kora.guide.grpcserver

import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest

@KoraAppTest(Application::class)
class GrpcServerAppTest {
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
        val created = createUser(newStub(), "Alice", "alice@example.com")

        assertEquals("Alice", created.name)
        assertEquals("alice@example.com", created.email)
    }

    @Test
    fun getsUserOverGrpc() {
        val stub = newStub()
        val created = createUser(stub, "Alice", "alice@example.com")

        val loaded = stub.getUser(GetUserRequest.newBuilder().setUserId(created.id).build())

        assertEquals(created.id, loaded.id)
        assertEquals("Alice", loaded.name)
        assertEquals("alice@example.com", loaded.email)
    }

    @Test
    fun listsUsersOverGrpc() {
        val stub = newStub()
        createUser(stub, "Alice", "alice@example.com")
        createUser(stub, "Bob", "bob@example.com")

        val listed = stub.getUsers(GetUsersRequest.newBuilder().setPage(0).setSize(10).setSort("name").build())

        assertEquals(2, listed.usersCount)
    }

    @Test
    fun updatesUserOverGrpc() {
        val stub = newStub()
        val created = createUser(stub, "Alice", "alice@example.com")

        val updated = stub.updateUser(
            UpdateUserRequest.newBuilder()
                .setUserId(created.id)
                .setName("Alice Updated")
                .setEmail("alice.updated@example.com")
                .build()
        )

        assertEquals("Alice Updated", updated.name)
        assertEquals("alice.updated@example.com", updated.email)
    }

    @Test
    fun deletesUserOverGrpc() {
        val stub = newStub()
        val created = createUser(stub, "Alice", "alice@example.com")

        val deleted = stub.deleteUser(DeleteUserRequest.newBuilder().setUserId(created.id).build())

        assertEquals(Empty.getDefaultInstance(), deleted)
    }

    @Test
    fun returnsNotFoundForMissingUser() {
        val error = assertThrows(StatusRuntimeException::class.java) {
            newStub().getUser(GetUserRequest.newBuilder().setUserId("missing").build())
        }

        assertEquals(Status.Code.NOT_FOUND, error.status.code)
    }

    private fun newStub(): UserServiceGrpc.UserServiceBlockingStub = UserServiceGrpc.newBlockingStub(channel)

    private fun createUser(stub: UserServiceGrpc.UserServiceBlockingStub, name: String, email: String): UserResponse =
        stub.createUser(CreateUserRequest.newBuilder().setName(name).setEmail(email).build())

    companion object {
        private const val GRPC_PORT = 8091
    }
}
