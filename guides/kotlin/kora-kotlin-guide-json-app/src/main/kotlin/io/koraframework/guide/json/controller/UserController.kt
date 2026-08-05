package io.koraframework.guide.json.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.json.dto.UserRequest
import io.koraframework.guide.json.dto.UserResponse
import io.koraframework.guide.json.dto.UserResult
import io.koraframework.guide.json.service.UserService
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json

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
