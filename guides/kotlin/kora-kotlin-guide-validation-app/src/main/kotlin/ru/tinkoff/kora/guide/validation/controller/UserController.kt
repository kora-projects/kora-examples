package ru.tinkoff.kora.guide.validation.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.validation.dto.UserRequest
import ru.tinkoff.kora.guide.validation.dto.UserResponse
import ru.tinkoff.kora.guide.validation.service.UserService
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
import ru.tinkoff.kora.validation.common.annotation.*
import java.time.Instant

@Component
@HttpController
open class UserController(
    private val userService: UserService
) {

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    @Validate
    open fun getUser(@Path @NotBlank @Pattern("^\\d+$") userId: String): UserResponse {
        return userService.getUser(userId)
            ?: throw HttpServerResponseException.of(404, "User not found")
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    @Validate
    open fun getUsers(
        @Query("page") @Range(from = 0.0, to = 1_000.0) page: Int?,
        @Query("size") @Range(from = 1.0, to = 100.0) size: Int?,
        @Query("sort") @Pattern("^(?i)(name|email|createdat)$") sort: String?
    ): List<UserResponse> {
        val pageNum = page ?: 0
        val pageSize = size ?: 10
        val sortBy = sort ?: "name"
        return userService.getUsers(pageNum, pageSize, sortBy)
    }

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    @Validate
    open fun createUser(@Valid @Json request: UserRequest): HttpResponseEntity<UserResponse> {
        val user = userService.createUser(request)
        return HttpResponseEntity.of(201, HttpHeaders.of(), user)
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/users/{userId}")
    @Json
    @Validate
    open fun updateUser(
        @Path @NotBlank @Pattern("^\\d+$") userId: String,
        @Valid @Json request: UserRequest
    ): HttpResponseEntity<UserResponse> {
        val updated = userService.updateUser(userId, request)
        return HttpResponseEntity.of(200, HttpHeaders.of("X-Updated-At", Instant.now().toString()), updated)
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    @Validate
    open fun deleteUser(@Path @NotBlank @Pattern("^\\d+$") userId: String): HttpServerResponse {
        userService.deleteUser(userId)
        return HttpServerResponse.of(204, HttpBody.empty())
    }
}
