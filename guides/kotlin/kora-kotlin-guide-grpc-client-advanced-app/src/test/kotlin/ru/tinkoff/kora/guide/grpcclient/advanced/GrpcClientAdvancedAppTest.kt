package ru.tinkoff.kora.guide.grpcclient.advanced

import com.google.protobuf.Empty
import com.google.protobuf.Timestamp
import io.grpc.*
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.stub.StreamObserver
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserRequest
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserUpdateRequest
import ru.tinkoff.kora.guide.grpcclient.advanced.grpc.LoggingInterceptor
import ru.tinkoff.kora.guide.grpcclient.advanced.grpc.UserStreamingAuthConfig
import ru.tinkoff.kora.guide.grpcclient.advanced.grpc.UserStreamingAuthInterceptor
import ru.tinkoff.kora.guide.grpcclient.advanced.service.UserStreamingClientService
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUsersResponse
import ru.tinkoff.kora.guide.grpcserver.advanced.UpdateUserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse
import ru.tinkoff.kora.guide.grpcserver.advanced.UserStreamingServiceGrpc
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class GrpcClientAdvancedAppTest {
    private lateinit var server: Server
    private lateinit var channel: ManagedChannel
    private lateinit var userStreamingClientService: UserStreamingClientService

    @BeforeEach
    fun setUp() {
        val service = ServerInterceptors.intercept(FakeUserStreamingService(), FakeAuthServerInterceptor())
        val serverName = InProcessServerBuilder.generateName()
        server = InProcessServerBuilder.forName(serverName).directExecutor().addService(service).build().start()
        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build()

        val authConfig = object : UserStreamingAuthConfig {
            override fun value(): String = "test-api-key"
        }
        val interceptedChannel =
            ClientInterceptors.intercept(channel, UserStreamingAuthInterceptor(authConfig), LoggingInterceptor())
        userStreamingClientService = UserStreamingClientService(
            UserStreamingServiceGrpc.newBlockingStub(interceptedChannel),
            UserStreamingServiceGrpc.newStub(interceptedChannel)
        )
    }

    @AfterEach
    fun tearDown() {
        channel.shutdownNow()
        server.shutdownNow()
    }

    @Test
    fun createUsersReturnsCreatedIdsFromInProcessGrpcServer() {
        val unique = UUID.randomUUID().toString().substring(0, 8)

        val result = userStreamingClientService.createUsers(
            listOf(
                UserRequest("Alice $unique", "alice-$unique@example.com"),
                UserRequest("Bob $unique", "bob-$unique@example.com")
            )
        )

        assertEquals(2, result.createdCount)
        assertEquals(2, result.userIds.size)
    }

    @Test
    fun getAllUsersReturnsStreamedUsersFromInProcessGrpcServer() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val created = userStreamingClientService.createUsers(
            listOf(
                UserRequest("Stream Alice $unique", "stream-alice-$unique@example.com"),
                UserRequest("Stream Bob $unique", "stream-bob-$unique@example.com")
            )
        )

        val users = userStreamingClientService.getAllUsers()

        assertTrue(created.userIds.all { id -> users.any { user -> user.id == id } })
    }

    @Test
    fun updateUsersReturnsUpdatedUsersFromInProcessGrpcServer() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val created = userStreamingClientService.createUsers(
            listOf(
                UserRequest("Original Alice $unique", "original-alice-$unique@example.com"),
                UserRequest("Original Bob $unique", "original-bob-$unique@example.com")
            )
        )

        val updated = userStreamingClientService.updateUsers(
            listOf(
                UserUpdateRequest(created.userIds[0], "Updated Alice $unique", "updated-alice-$unique@example.com"),
                UserUpdateRequest(created.userIds[1], "Updated Bob $unique", "updated-bob-$unique@example.com")
            )
        )

        assertTrue(updated.any { it.name == "Updated Alice $unique" })
        assertTrue(updated.any { it.name == "Updated Bob $unique" })
    }

    @Test
    fun rejectsStreamingCallsWithoutApiKeyAgainstInProcessGrpcServer() {
        val unauthenticatedService = UserStreamingClientService(
            UserStreamingServiceGrpc.newBlockingStub(channel),
            UserStreamingServiceGrpc.newStub(channel)
        )

        val exception = assertThrows(StatusRuntimeException::class.java) { unauthenticatedService.getAllUsers() }

        assertEquals(Status.Code.UNAUTHENTICATED, exception.status.code)
    }

    private class FakeAuthServerInterceptor : ServerInterceptor {
        override fun <ReqT : Any?, RespT : Any?> interceptCall(
            call: ServerCall<ReqT, RespT>,
            headers: Metadata,
            next: ServerCallHandler<ReqT, RespT>
        ): ServerCall.Listener<ReqT> {
            val authorization = headers.get(AUTHORIZATION_HEADER)
            if (authorization != "test-api-key") {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid API key"), Metadata())
                return object : ServerCall.Listener<ReqT>() {}
            }
            return Contexts.interceptCall(
                Context.current().withValue(AUTHORIZATION_CONTEXT_KEY, authorization),
                call,
                headers,
                next
            )
        }
    }

    private class FakeUserStreamingService : UserStreamingServiceGrpc.UserStreamingServiceImplBase() {
        private val users = CopyOnWriteArrayList<StoredUser>()

        override fun createUsers(responseObserver: StreamObserver<CreateUsersResponse>): StreamObserver<CreateUserRequest> {
            val createdIds = CopyOnWriteArrayList<String>()
            return object : StreamObserver<CreateUserRequest> {
                override fun onNext(value: CreateUserRequest) {
                    val user = StoredUser(
                        UUID.randomUUID().toString(),
                        value.name,
                        value.email,
                        LocalDateTime.now(ZoneOffset.UTC)
                    )
                    users += user
                    createdIds += user.id
                }

                override fun onError(t: Throwable) = responseObserver.onError(t)

                override fun onCompleted() {
                    responseObserver.onNext(
                        CreateUsersResponse.newBuilder().setCreatedCount(createdIds.size).addAllUserIds(createdIds)
                            .build()
                    )
                    responseObserver.onCompleted()
                }
            }
        }

        override fun getAllUsers(request: Empty, responseObserver: StreamObserver<UserResponse>) {
            users.forEach { responseObserver.onNext(toGrpc(it)) }
            responseObserver.onCompleted()
        }

        override fun updateUsers(responseObserver: StreamObserver<UserResponse>): StreamObserver<UpdateUserRequest> =
            object : StreamObserver<UpdateUserRequest> {
                override fun onNext(value: UpdateUserRequest) {
                    val index = users.indexOfFirst { it.id == value.userId }
                    if (index < 0) {
                        responseObserver.onError(
                            Status.NOT_FOUND.withDescription("User not found").asRuntimeException()
                        )
                        return
                    }
                    val updated = users[index].copy(name = value.name, email = value.email)
                    users[index] = updated
                    responseObserver.onNext(toGrpc(updated))
                }

                override fun onError(t: Throwable) = responseObserver.onError(t)

                override fun onCompleted() = responseObserver.onCompleted()
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

    companion object {
        private val AUTHORIZATION_HEADER: Metadata.Key<String> =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
        private val AUTHORIZATION_CONTEXT_KEY: Context.Key<String> = Context.key("authorization")
    }
}
