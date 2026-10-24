package io.koraframework.guide.validation.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.validation.dto.UserRequest
import io.koraframework.guide.validation.dto.UserResponse
import io.koraframework.guide.validation.service.UserService
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.common.annotation.Query
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.header.HttpHeaders
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json
import io.koraframework.validation.common.annotation.*
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
