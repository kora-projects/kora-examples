package ru.tinkoff.kora.guide.openapi.httpserver.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.openapi.httpserver.dto.UserRequest
import ru.tinkoff.kora.guide.openapi.httpserver.dto.UserResponse
import ru.tinkoff.kora.guide.openapi.httpserver.service.UserService
import ru.tinkoff.kora.guide.openapi.httpserver.user.api.UsersApiDelegate
import ru.tinkoff.kora.guide.openapi.httpserver.user.api.UsersApiResponses
import ru.tinkoff.kora.guide.openapi.httpserver.user.model.ErrorResponseTO
import ru.tinkoff.kora.guide.openapi.httpserver.user.model.UserRequestTO
import ru.tinkoff.kora.guide.openapi.httpserver.user.model.UserResponseTO
import java.time.Instant
import java.time.ZoneOffset

@Component
class UserApiDelegateImpl(
    private val userService: UserService
) : UsersApiDelegate {

    override fun createUser(userRequest: UserRequestTO): UsersApiResponses.CreateUserApiResponse {
        val created = userService.createUser(UserRequest(userRequest.name, userRequest.email))
        return UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse(toGenerated(created))
    }

    override fun deleteUser(userId: String): UsersApiResponses.DeleteUserApiResponse {
        if (userService.getUser(userId) == null) {
            return UsersApiResponses.DeleteUserApiResponse.DeleteUser404ApiResponse(
                notFound(userId)
            )
        }

        userService.deleteUser(userId)
        return UsersApiResponses.DeleteUserApiResponse.DeleteUser204ApiResponse()
    }

    override fun getUser(userId: String): UsersApiResponses.GetUserApiResponse {
        val user = userService.getUser(userId)
        return if (user == null) {
            UsersApiResponses.GetUserApiResponse.GetUser404ApiResponse(notFound(userId))
        } else {
            UsersApiResponses.GetUserApiResponse.GetUser200ApiResponse(toGenerated(user))
        }
    }

    override fun getUsers(page: Int?, size: Int?, sort: String?): UsersApiResponses.GetUsersApiResponse {
        val effectivePage = page ?: 0
        val effectiveSize = size ?: 10
        val effectiveSort = sort ?: "name"
        val users = userService.getUsers(effectivePage, effectiveSize, effectiveSort)
            .map(::toGenerated)
        return UsersApiResponses.GetUsersApiResponse.GetUsers200ApiResponse(users)
    }

    override fun updateUser(userId: String, userRequest: UserRequestTO): UsersApiResponses.UpdateUserApiResponse {
        if (userService.getUser(userId) == null) {
            return UsersApiResponses.UpdateUserApiResponse.UpdateUser404ApiResponse(
                notFound(userId)
            )
        }

        val updated = userService.updateUser(userId, UserRequest(userRequest.name, userRequest.email))
        return UsersApiResponses.UpdateUserApiResponse.UpdateUser200ApiResponse(
            toGenerated(updated),
            Instant.now().toString()
        )
    }

    private fun toGenerated(user: UserResponse): UserResponseTO {
        return UserResponseTO(
            user.id,
            user.name,
            user.email,
            user.createdAt.atOffset(ZoneOffset.UTC)
        )
    }

    private fun notFound(userId: String): ErrorResponseTO {
        return ErrorResponseTO("User with id $userId was not found")
    }
}
