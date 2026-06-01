package ru.tinkoff.kora.guide.grpcserver.grpc

import com.google.protobuf.Empty
import com.google.protobuf.Timestamp
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.grpcserver.*
import ru.tinkoff.kora.guide.grpcserver.dto.UserRequest
import ru.tinkoff.kora.guide.grpcserver.dto.UserResponse
import ru.tinkoff.kora.guide.grpcserver.service.UserNotFoundException
import ru.tinkoff.kora.guide.grpcserver.service.UserService
import java.time.ZoneOffset

@Component
class UserServiceGrpcHandler(
    private val userService: UserService
) : UserServiceGrpc.UserServiceImplBase() {

    private val logger = LoggerFactory.getLogger(UserServiceGrpcHandler::class.java)

    override fun createUser(
        request: CreateUserRequest,
        responseObserver: StreamObserver<ru.tinkoff.kora.guide.grpcserver.UserResponse>
    ) {
        try {
            logger.info("Creating user: name={}, email={}", request.name, request.email)
            val user = userService.createUser(UserRequest(request.name, request.email))
            responseObserver.onNext(toGrpcUser(user))
            responseObserver.onCompleted()
        } catch (e: Exception) {
            logger.error("Failed to create user", e)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Failed to create user").withCause(e).asRuntimeException()
            )
        }
    }

    override fun getUser(
        request: GetUserRequest,
        responseObserver: StreamObserver<ru.tinkoff.kora.guide.grpcserver.UserResponse>
    ) {
        try {
            logger.info("Getting user: id={}", request.userId)
            val user = userService.getUser(request.userId)
                ?: throw Status.NOT_FOUND.withDescription("User not found: ${request.userId}").asRuntimeException()
            responseObserver.onNext(toGrpcUser(user))
            responseObserver.onCompleted()
        } catch (e: RuntimeException) {
            logger.error("Failed to get user", e)
            responseObserver.onError(e)
        }
    }

    override fun getUsers(request: GetUsersRequest, responseObserver: StreamObserver<GetUsersResponse>) {
        try {
            val page = request.page
            val size = if (request.size == 0) 10 else request.size
            val sort = request.sort.ifBlank { "name" }
            val response = GetUsersResponse.newBuilder()
                .addAllUsers(userService.getUsers(page, size, sort).map(::toGrpcUser))
                .build()
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            logger.error("Failed to get users", e)
            responseObserver.onError(
                Status.INTERNAL.withDescription("Failed to get users").withCause(e).asRuntimeException()
            )
        }
    }

    override fun updateUser(
        request: UpdateUserRequest,
        responseObserver: StreamObserver<ru.tinkoff.kora.guide.grpcserver.UserResponse>
    ) {
        try {
            val updated = userService.updateUser(request.userId, UserRequest(request.name, request.email))
            responseObserver.onNext(toGrpcUser(updated))
            responseObserver.onCompleted()
        } catch (e: UserNotFoundException) {
            logger.error("Failed to update user", e)
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.message).asRuntimeException())
        }
    }

    override fun deleteUser(request: DeleteUserRequest, responseObserver: StreamObserver<Empty>) {
        try {
            userService.deleteUser(request.userId)
            responseObserver.onNext(Empty.getDefaultInstance())
            responseObserver.onCompleted()
        } catch (e: UserNotFoundException) {
            logger.error("Failed to delete user", e)
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.message).asRuntimeException())
        }
    }

    private fun toGrpcUser(user: UserResponse): ru.tinkoff.kora.guide.grpcserver.UserResponse {
        return ru.tinkoff.kora.guide.grpcserver.UserResponse.newBuilder()
            .setId(user.id)
            .setName(user.name)
            .setEmail(user.email)
            .setCreatedAt(
                Timestamp.newBuilder()
                    .setSeconds(user.createdAt.toEpochSecond(ZoneOffset.UTC))
                    .setNanos(user.createdAt.nano)
                    .build()
            )
            .build()
    }
}
