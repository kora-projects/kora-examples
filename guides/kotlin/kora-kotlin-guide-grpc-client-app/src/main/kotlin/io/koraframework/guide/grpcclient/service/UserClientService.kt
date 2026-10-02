package io.koraframework.guide.grpcclient.service

import io.koraframework.common.annotation.Component
import io.koraframework.guide.grpcclient.dto.UserRequest
import io.koraframework.guide.grpcclient.dto.UserResponse
import io.koraframework.guide.grpcserver.*
import java.time.LocalDateTime
import java.time.ZoneOffset

@Component
class UserClientService(
    private val userService: UserServiceGrpc.UserServiceBlockingStub
) {

    fun createUser(request: UserRequest): UserResponse {
        return toDto(
            userService.createUser(
                CreateUserRequest.newBuilder()
                    .setName(request.name)
                    .setEmail(request.email)
                    .build()
            )
        )
    }

    fun getUser(userId: String): UserResponse {
        return toDto(userService.getUser(GetUserRequest.newBuilder().setUserId(userId).build()))
    }

    fun getUsers(page: Int, size: Int, sort: String): List<UserResponse> {
        return userService.getUsers(
            GetUsersRequest.newBuilder()
                .setPage(page)
                .setSize(size)
                .setSort(sort)
                .build()
        ).usersList.map(::toDto)
    }

    fun updateUser(userId: String, request: UserRequest): UserResponse {
        return toDto(
            userService.updateUser(
                UpdateUserRequest.newBuilder()
                    .setUserId(userId)
                    .setName(request.name)
                    .setEmail(request.email)
                    .build()
            )
        )
    }

    fun deleteUser(userId: String) {
        userService.deleteUser(DeleteUserRequest.newBuilder().setUserId(userId).build())
    }

    private fun toDto(response: io.koraframework.guide.grpcserver.UserResponse): UserResponse {
        return UserResponse(
            response.id,
            response.name,
            response.email,
            LocalDateTime.ofEpochSecond(response.createdAt.seconds, response.createdAt.nanos, ZoneOffset.UTC)
        )
    }
}
