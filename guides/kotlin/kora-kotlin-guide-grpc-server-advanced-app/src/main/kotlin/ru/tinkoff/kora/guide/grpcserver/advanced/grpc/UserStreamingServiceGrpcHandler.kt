package ru.tinkoff.kora.guide.grpcserver.advanced.grpc

import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.CreateUsersResponse
import ru.tinkoff.kora.guide.grpcserver.advanced.UpdateUserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.UserStreamingServiceGrpc
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.service.UserStreamingService

@Component
class UserStreamingServiceGrpcHandler(
    private val userStreamingService: UserStreamingService
) : UserStreamingServiceGrpc.UserStreamingServiceImplBase() {

    private val logger = LoggerFactory.getLogger(UserStreamingServiceGrpcHandler::class.java)

    override fun getAllUsers(
        request: Empty,
        responseObserver: StreamObserver<ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse>
    ) {
        try {
            userStreamingService.getAllUsers().forEach { responseObserver.onNext(it.toGrpcUser()) }
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Failed to stream users").withCause(e).asRuntimeException()
            )
        }
    }

    override fun createUsers(responseObserver: StreamObserver<CreateUsersResponse>): StreamObserver<CreateUserRequest> {
        return object : StreamObserver<CreateUserRequest> {
            private val requests = mutableListOf<UserRequest>()

            override fun onNext(value: CreateUserRequest) {
                requests += UserRequest(value.name, value.email)
            }

            override fun onError(t: Throwable) {
                logger.error("Client streaming failed", t)
                responseObserver.onError(t)
            }

            override fun onCompleted() {
                try {
                    val createdUsers = userStreamingService.createUsers(requests)
                    responseObserver.onNext(
                        CreateUsersResponse.newBuilder()
                            .setCreatedCount(createdUsers.size)
                            .addAllUserIds(createdUsers.map { it.id })
                            .build()
                    )
                    responseObserver.onCompleted()
                } catch (e: Exception) {
                    responseObserver.onError(
                        Status.INTERNAL.withDescription("Failed to create users").withCause(e).asRuntimeException()
                    )
                }
            }
        }
    }

    override fun updateUsers(responseObserver: StreamObserver<ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse>): StreamObserver<UpdateUserRequest> {
        return object : StreamObserver<UpdateUserRequest> {
            override fun onNext(value: UpdateUserRequest) {
                try {
                    val user = userStreamingService.tryUpdateUser(value.userId, UserRequest(value.name, value.email))
                        ?: throw Status.NOT_FOUND.withDescription("User not found: ${value.userId}")
                            .asRuntimeException()
                    responseObserver.onNext(user.toGrpcUser())
                } catch (e: StatusRuntimeException) {
                    responseObserver.onError(e)
                }
            }

            override fun onError(t: Throwable) {
                logger.error("Bidirectional streaming failed", t)
                responseObserver.onError(t)
            }

            override fun onCompleted() {
                responseObserver.onCompleted()
            }
        }
    }
}
