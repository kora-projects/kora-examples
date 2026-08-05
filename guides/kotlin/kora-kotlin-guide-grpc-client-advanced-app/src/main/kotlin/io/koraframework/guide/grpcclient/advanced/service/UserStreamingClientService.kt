package io.koraframework.guide.grpcclient.advanced.service

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import io.koraframework.common.annotation.Component
import io.koraframework.guide.grpcclient.advanced.dto.UserRequest
import io.koraframework.guide.grpcclient.advanced.dto.UserResponse
import io.koraframework.guide.grpcclient.advanced.dto.UserUpdateRequest
import io.koraframework.guide.grpcserver.advanced.CreateUserRequest
import io.koraframework.guide.grpcserver.advanced.CreateUsersResponse
import io.koraframework.guide.grpcserver.advanced.UpdateUserRequest
import io.koraframework.guide.grpcserver.advanced.UserStreamingServiceGrpc
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

@Component
class UserStreamingClientService(
    private val blockingStub: UserStreamingServiceGrpc.UserStreamingServiceBlockingStub,
    private val asyncStub: UserStreamingServiceGrpc.UserStreamingServiceStub
) {

    fun createUsers(requests: List<UserRequest>): CreateUsersResult {
        val future = CompletableFuture<CreateUsersResult>()
        val responseObserver = object : StreamObserver<CreateUsersResponse> {
            override fun onNext(value: CreateUsersResponse) {
                future.complete(CreateUsersResult(value.createdCount, value.userIdsList.toList()))
            }

            override fun onError(t: Throwable) {
                future.completeExceptionally(t)
            }

            override fun onCompleted() = Unit
        }

        val requestObserver = asyncStub.createUsers(responseObserver)
        try {
            requests.forEach { request ->
                requestObserver.onNext(
                    CreateUserRequest.newBuilder()
                        .setName(request.name)
                        .setEmail(request.email)
                        .build()
                )
            }
            requestObserver.onCompleted()
            return future.get(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            requestObserver.onError(e)
            throw RuntimeException("Failed to create users over gRPC streaming", e)
        }
    }

    fun getAllUsers(): List<UserResponse> {
        val users = mutableListOf<UserResponse>()
        val iterator = blockingStub.getAllUsers(Empty.getDefaultInstance())
        iterator.forEachRemaining { user -> users += toDto(user) }
        return users
    }

    fun updateUsers(updates: List<UserUpdateRequest>): List<UserResponse> {
        val future = CompletableFuture<List<UserResponse>>()
        val responses = CopyOnWriteArrayList<UserResponse>()
        val responseObserver = object : StreamObserver<io.koraframework.guide.grpcserver.advanced.UserResponse> {
            override fun onNext(value: io.koraframework.guide.grpcserver.advanced.UserResponse) {
                responses += toDto(value)
            }

            override fun onError(t: Throwable) {
                future.completeExceptionally(t)
            }

            override fun onCompleted() {
                future.complete(responses.toList())
            }
        }

        val requestObserver = asyncStub.updateUsers(responseObserver)
        try {
            updates.forEach { update ->
                requestObserver.onNext(
                    UpdateUserRequest.newBuilder()
                        .setUserId(update.userId)
                        .setName(update.name)
                        .setEmail(update.email)
                        .build()
                )
            }
            requestObserver.onCompleted()
            return future.get(5, TimeUnit.SECONDS)
        } catch (e: Exception) {
            requestObserver.onError(e)
            throw RuntimeException("Failed to update users over gRPC streaming", e)
        }
    }

    private fun toDto(response: io.koraframework.guide.grpcserver.advanced.UserResponse): UserResponse {
        return UserResponse(
            response.id,
            response.name,
            response.email,
            LocalDateTime.ofEpochSecond(response.createdAt.seconds, response.createdAt.nanos, ZoneOffset.UTC)
        )
    }

    data class CreateUsersResult(val createdCount: Int, val userIds: List<String>)
}
