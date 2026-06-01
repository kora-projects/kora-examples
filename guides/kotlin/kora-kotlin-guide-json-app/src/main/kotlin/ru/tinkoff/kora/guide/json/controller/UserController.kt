package ru.tinkoff.kora.guide.json.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.json.dto.UserRequest
import ru.tinkoff.kora.guide.json.dto.UserResponse
import ru.tinkoff.kora.guide.json.dto.UserResult
import ru.tinkoff.kora.guide.json.service.UserService
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Path
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class UserController(
    private val userService: UserService
) {

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    fun createUser(@Json request: UserRequest): UserResponse {
        return userService.createUser(request)
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    fun getAllUsers(): List<UserResponse> {
        return userService.getAllUsers()
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users/{id}")
    @Json
    fun getUser(@Path id: String): UserResult {
        return userService.getUser(id)
    }
}
