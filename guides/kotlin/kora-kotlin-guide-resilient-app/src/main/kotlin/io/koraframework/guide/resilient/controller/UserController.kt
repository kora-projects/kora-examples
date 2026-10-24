package io.koraframework.guide.resilient.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.resilient.dto.UserRequest
import io.koraframework.guide.resilient.dto.UserResponse
import io.koraframework.guide.resilient.service.UserService
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.*
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.header.HttpHeaders
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json
import java.time.Instant

@Component
@HttpController
class UserController(private val userService: UserService) {

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    fun getUser(@Path userId: String): UserResponse =
        userService.getUser(userId) ?: throw HttpServerResponseException.of(404, "User not found")

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    fun getUsers(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("sort") sort: String?
    ): List<UserResponse> =
        userService.getUsers(page ?: 0, size ?: 10, sort ?: "name")

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    fun createUser(
        @Json request: UserRequest,
        @Header("X-Request-ID") requestId: String?,
        @Header("User-Agent") userAgent: String?,
        @Cookie("sessionId") sessionId: String?
    ): HttpResponseEntity<UserResponse> =
        HttpResponseEntity.of(201, HttpHeaders.of(), userService.createUser(request))

    @HttpRoute(method = HttpMethod.PUT, path = "/users/{userId}")
    @Json
    fun updateUser(@Path userId: String, @Json request: UserRequest): HttpResponseEntity<UserResponse> =
        HttpResponseEntity.of(
            200,
            HttpHeaders.of("X-Updated-At", Instant.now().toString()),
            userService.updateUser(userId, request)
        )

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    fun deleteUser(@Path userId: String): HttpServerResponse {
        userService.deleteUser(userId)
        return HttpServerResponse.of(204, HttpBody.empty())
    }
}
