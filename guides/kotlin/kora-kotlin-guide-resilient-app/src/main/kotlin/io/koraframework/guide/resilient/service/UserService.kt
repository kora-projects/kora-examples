package io.koraframework.guide.resilient.service

import io.koraframework.common.annotation.Component
import io.koraframework.guide.resilient.dto.UserRequest
import io.koraframework.guide.resilient.dto.UserResponse
import io.koraframework.guide.resilient.repository.UserRepository
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.resilient.circuitbreaker.annotation.CircuitBreakable
import io.koraframework.resilient.fallback.annotation.Fallback
import io.koraframework.resilient.retry.annotation.Retryable
import io.koraframework.resilient.timeout.annotation.Timeout
import java.time.LocalDateTime

@Component
open class UserService(private val userRepository: UserRepository) {

    @Fallback(value = "default", method = "createUserFallback(request)")
    open fun createUser(request: UserRequest): UserResponse {
        val generatedId = userRepository.save(request.name, request.email)
        return UserResponse(generatedId, request.name, request.email, LocalDateTime.now())
    }

    @Retryable(DefaultRetry::class)
    open fun getUser(id: String): UserResponse? = userRepository.findById(id)

    @CircuitBreakable(DefaultCircuitBreaker::class)
    @Retryable(DefaultRetry::class)
    @Timeout(DefaultTimeouter::class)
    open fun getUsers(page: Int, size: Int, sort: String): List<UserResponse> =
        userRepository.findAll()
            .sortedWith(getComparator(sort))
            .drop(page * size)
            .take(size)

    @CircuitBreakable(DefaultCircuitBreaker::class)
    open fun updateUser(id: String, request: UserRequest): UserResponse {
        if (!userRepository.update(id, request.name, request.email)) {
            throw HttpServerResponseException.of(404, "User not found")
        }
        return UserResponse(id, request.name, request.email, LocalDateTime.now())
    }

    @Timeout(DefaultTimeouter::class)
    open fun deleteUser(id: String) {
        if (!userRepository.deleteById(id)) {
            throw HttpServerResponseException.of(404, "User not found")
        }
    }

    protected open fun createUserFallback(request: UserRequest): UserResponse =
        UserResponse("pending-file-write", request.name, request.email, LocalDateTime.now())

    private fun getComparator(sort: String): Comparator<UserResponse> = when (sort.lowercase()) {
        "name" -> compareBy { it.name }
        "email" -> compareBy { it.email }
        "createdat" -> compareBy { it.createdAt }
        else -> compareBy { it.name }
    }
}
