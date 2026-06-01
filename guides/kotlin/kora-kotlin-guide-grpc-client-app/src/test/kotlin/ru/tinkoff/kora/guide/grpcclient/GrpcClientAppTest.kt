package ru.tinkoff.kora.guide.grpcclient

import com.google.protobuf.Empty
import com.google.protobuf.Timestamp
import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.stub.StreamObserver
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.grpcclient.dto.UserRequest
import ru.tinkoff.kora.guide.grpcclient.service.UserClientService
import ru.tinkoff.kora.guide.grpcserver.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class GrpcClientAppTest {
    private lateinit var server: Server
    private lateinit var channel: ManagedChannel
    private lateinit var userClientService: UserClientService

    @BeforeEach
    fun setUp() {
        val serverName = InProcessServerBuilder.generateName()
        server = InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(FakeUserService())
            .build()
            .start()
        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build()
        userClientService = UserClientService(UserServiceGrpc.newBlockingStub(channel))
    }

    @AfterEach
    fun tearDown() {
        channel.shutdownNow()
        server.shutdownNow()
    }

    @Test
    fun createUserReturnsCreatedUserFromInProcessGrpcServer() {
        val unique = UUID.randomUUID().toString().substring(0, 8)

        val created = userClientService.createUser(UserRequest("Client User $unique", "client-$unique@example.com"))

        assertEquals("Client User $unique", created.name)
        assertTrue(created.id.isNotBlank())
    }

    @Test
    fun getUserReturnsUserFromInProcessGrpcServer() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val created = userClientService.createUser(UserRequest("Lookup User $unique", "lookup-$unique@example.com"))

        val user = userClientService.getUser(created.id)

        assertEquals(created.id, user.id)
        assertEquals("Lookup User $unique", user.name)
    }

    @Test
    fun getMissingUserThrowsAgainstInProcessGrpcServer() {
        val exception = assertThrows(StatusRuntimeException::class.java) { userClientService.getUser("missing-user") }

        assertEquals(Status.Code.NOT_FOUND, exception.status.code)
    }

    @Test
    fun getUsersReturnsSortedUsersFromInProcessGrpcServer() {
        val suffix = UUID.randomUUID().toString().substring(0, 6)
        val userA = userClientService.createUser(UserRequest("Alpha $suffix", "a-$suffix@example.com"))
        val userB = userClientService.createUser(UserRequest("Bravo $suffix", "b-$suffix@example.com"))
        val userC = userClientService.createUser(UserRequest("Charlie $suffix", "c-$suffix@example.com"))

        val users = userClientService.getUsers(0, 100, "name")
        val ids = setOf(userA.id, userB.id, userC.id)
        val filtered = users.filter { it.id in ids }

        assertEquals(listOf("Alpha $suffix", "Bravo $suffix", "Charlie $suffix"), filtered.map { it.name })
    }

    @Test
    fun updateUserReturnsUpdatedUserFromInProcessGrpcServer() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val created = userClientService.createUser(UserRequest("Original User $unique", "original-$unique@example.com"))

        val updated =
            userClientService.updateUser(created.id, UserRequest("Updated User $unique", "updated-$unique@example.com"))

        assertEquals(created.id, updated.id)
        assertEquals("Updated User $unique", updated.name)
        assertEquals("updated-$unique@example.com", updated.email)
    }

    @Test
    fun deleteUserRemovesUserFromInProcessGrpcServer() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val created = userClientService.createUser(UserRequest("Delete User $unique", "delete-$unique@example.com"))

        userClientService.deleteUser(created.id)

        val exception = assertThrows(StatusRuntimeException::class.java) { userClientService.getUser(created.id) }
        assertEquals(Status.Code.NOT_FOUND, exception.status.code)
    }

    private class FakeUserService : UserServiceGrpc.UserServiceImplBase() {
        private val users = linkedMapOf<String, StoredUser>()

        override fun createUser(request: CreateUserRequest, responseObserver: StreamObserver<UserResponse>) {
            val user =
                StoredUser(UUID.randomUUID().toString(), request.name, request.email, LocalDateTime.now(ZoneOffset.UTC))
            users[user.id] = user
            responseObserver.onNext(toGrpc(user))
            responseObserver.onCompleted()
        }

        override fun getUser(request: GetUserRequest, responseObserver: StreamObserver<UserResponse>) {
            val user = users[request.userId]
            if (user == null) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException())
                return
            }
            responseObserver.onNext(toGrpc(user))
            responseObserver.onCompleted()
        }

        override fun getUsers(request: GetUsersRequest, responseObserver: StreamObserver<GetUsersResponse>) {
            val sorted = users.values.sortedBy { if (request.sort == "name") it.name else it.id }
            val fromIndex = minOf(request.page * request.size, sorted.size)
            val toIndex = minOf(fromIndex + request.size, sorted.size)
            responseObserver.onNext(
                GetUsersResponse.newBuilder().addAllUsers(sorted.subList(fromIndex, toIndex).map(::toGrpc)).build()
            )
            responseObserver.onCompleted()
        }

        override fun updateUser(request: UpdateUserRequest, responseObserver: StreamObserver<UserResponse>) {
            val existing = users[request.userId]
            if (existing == null) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("User not found").asRuntimeException())
                return
            }
            val updated = existing.copy(name = request.name, email = request.email)
            users[updated.id] = updated
            responseObserver.onNext(toGrpc(updated))
            responseObserver.onCompleted()
        }

        override fun deleteUser(request: DeleteUserRequest, responseObserver: StreamObserver<Empty>) {
            users.remove(request.userId)
            responseObserver.onNext(Empty.getDefaultInstance())
            responseObserver.onCompleted()
        }

        private fun toGrpc(user: StoredUser): UserResponse =
            UserResponse.newBuilder()
                .setId(user.id)
                .setName(user.name)
                .setEmail(user.email)
                .setCreatedAt(
                    Timestamp.newBuilder().setSeconds(user.createdAt.toEpochSecond(ZoneOffset.UTC))
                        .setNanos(user.createdAt.nano).build()
                )
                .build()
    }

    private data class StoredUser(val id: String, val name: String, val email: String, val createdAt: LocalDateTime)
}
