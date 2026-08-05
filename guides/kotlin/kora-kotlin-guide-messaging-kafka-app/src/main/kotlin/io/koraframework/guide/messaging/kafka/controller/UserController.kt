package io.koraframework.guide.messaging.kafka.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.messaging.kafka.dto.UserAcceptedResponse
import io.koraframework.guide.messaging.kafka.dto.UserRequest
import io.koraframework.guide.messaging.kafka.dto.UserResponse
import io.koraframework.guide.messaging.kafka.kafka.UserCreatedEvent
import io.koraframework.guide.messaging.kafka.kafka.UserCreatedPublisher
import io.koraframework.guide.messaging.kafka.service.UserService
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
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Component
@HttpController
class UserController(
    private val userCreatedPublisher: UserCreatedPublisher,
    private val userService: UserService
) {

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    fun createUser(@Json request: UserRequest): HttpResponseEntity<UserAcceptedResponse> {
        val userId = UUID.randomUUID().toString()
        val event = UserCreatedEvent(userId, request.name, request.email, LocalDateTime.now())
        userCreatedPublisher.send(event)
        return HttpResponseEntity.of(202, HttpHeaders.of(), UserAcceptedResponse(userId))
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    fun getUser(@Path userId: String): UserResponse {
        return userService.getUser(userId) ?: throw HttpServerResponseException.of(404, "User not found")
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

    @HttpRoute(method = HttpMethod.PUT, path = "/users/{userId}")
    @Json
    fun updateUser(@Path userId: String, @Json request: UserRequest): HttpResponseEntity<UserResponse> {
        val updated = userService.updateUser(userId, request)
        return HttpResponseEntity.of(200, HttpHeaders.of("X-Updated-At", Instant.now().toString()), updated)
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    fun deleteUser(@Path userId: String): HttpServerResponse {
        userService.deleteUser(userId)
        return HttpServerResponse.of(204, HttpBody.empty())
    }
}
