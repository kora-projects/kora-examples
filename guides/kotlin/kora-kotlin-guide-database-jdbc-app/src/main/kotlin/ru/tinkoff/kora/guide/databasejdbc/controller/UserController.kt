package ru.tinkoff.kora.guide.databasejdbc.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.databasejdbc.dto.UserRequest
import ru.tinkoff.kora.guide.databasejdbc.dto.UserResponse
import ru.tinkoff.kora.guide.databasejdbc.service.UserService
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.annotation.Path
import ru.tinkoff.kora.http.common.annotation.Query
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.common.header.HttpHeaders
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class UserController(
    private val userService: UserService
) {

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    fun getUser(@Path userId: String): UserResponse {
        return userService.getUser(userId)
            ?: throw HttpServerResponseException.of(404, "User not found")
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    fun getUsers(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("sort") sort: String?
    ): List<UserResponse> {
        return userService.getUsers(page ?: 0, size ?: 10, sort ?: "name")
    }

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    fun createUser(@Json request: UserRequest): HttpResponseEntity<UserResponse> {
        val user = userService.createUser(request)
        return HttpResponseEntity.of(201, HttpHeaders.of(), user)
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/users/{userId}")
    @Json
    fun updateUser(@Path userId: String, @Json request: UserRequest): UserResponse {
        return userService.updateUser(userId, request)
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    fun deleteUser(@Path userId: String): HttpServerResponse {
        userService.deleteUser(userId)
        return HttpServerResponse.of(204, HttpBody.empty())
    }
}
