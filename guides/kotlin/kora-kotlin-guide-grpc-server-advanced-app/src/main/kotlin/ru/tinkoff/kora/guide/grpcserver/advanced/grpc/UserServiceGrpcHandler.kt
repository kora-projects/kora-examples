package ru.tinkoff.kora.guide.grpcserver.advanced.grpc

import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.stub.StreamObserver
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.DeleteUserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.GetUserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.GetUsersRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.GetUsersResponse
import ru.tinkoff.kora.guide.grpcserver.advanced.UpdateUserRequestUnary
import ru.tinkoff.kora.guide.grpcserver.advanced.UserServiceGrpc
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.service.UserNotFoundException
import ru.tinkoff.kora.guide.grpcserver.advanced.service.UserService

@Component
class UserServiceGrpcHandler(
    private val userService: UserService
) : UserServiceGrpc.UserServiceImplBase() {

    override fun createUser(
        request: CreateUserRequest,
        responseObserver: StreamObserver<ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse>
    ) {
        try {
            responseObserver.onNext(userService.createUser(UserRequest(request.name, request.email)).toGrpcUser())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Failed to create user").withCause(e).asRuntimeException()
            )
        }
    }

    override fun getUser(
        request: GetUserRequest,
        responseObserver: StreamObserver<ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse>
    ) {
        try {
            val user = userService.getUser(request.userId)
                ?: throw Status.NOT_FOUND.withDescription("User not found: ${request.userId}").asRuntimeException()
            responseObserver.onNext(user.toGrpcUser())
            responseObserver.onCompleted()
        } catch (e: RuntimeException) {
            responseObserver.onError(e)
        }
    }

    override fun getUsers(request: GetUsersRequest, responseObserver: StreamObserver<GetUsersResponse>) {
        try {
            val size = if (request.size == 0) 10 else request.size
            val sort = request.sort.ifBlank { "name" }
            responseObserver.onNext(
                GetUsersResponse.newBuilder()
                    .addAllUsers(userService.getUsers(request.page, size, sort).map { it.toGrpcUser() })
                    .build()
            )
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Failed to get users").withCause(e).asRuntimeException()
            )
        }
    }

    override fun updateUser(
        request: UpdateUserRequestUnary,
        responseObserver: StreamObserver<ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse>
    ) {
        try {
            responseObserver.onNext(
                userService.updateUser(request.userId, UserRequest(request.name, request.email)).toGrpcUser()
            )
            responseObserver.onCompleted()
        } catch (e: UserNotFoundException) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.message).asRuntimeException())
        }
    }

    override fun deleteUser(request: DeleteUserRequest, responseObserver: StreamObserver<Empty>) {
        try {
            userService.deleteUser(request.userId)
            responseObserver.onNext(Empty.getDefaultInstance())
            responseObserver.onCompleted()
        } catch (e: UserNotFoundException) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.message).asRuntimeException())
        }
    }
}
