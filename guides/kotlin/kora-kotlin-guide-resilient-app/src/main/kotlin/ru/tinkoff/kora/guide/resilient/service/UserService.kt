package ru.tinkoff.kora.guide.resilient.service

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.resilient.dto.UserRequest
import ru.tinkoff.kora.guide.resilient.dto.UserResponse
import ru.tinkoff.kora.guide.resilient.repository.UserRepository
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker
import ru.tinkoff.kora.resilient.fallback.annotation.Fallback
import ru.tinkoff.kora.resilient.retry.annotation.Retry
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout
import java.time.LocalDateTime

@Component
open class UserService(private val userRepository: UserRepository) {

    @Fallback(value = "default", method = "createUserFallback(request)")
    open fun createUser(request: UserRequest): UserResponse {
        val generatedId = userRepository.save(request.name, request.email)
        return UserResponse(generatedId, request.name, request.email, LocalDateTime.now())
    }

    @Retry("default")
    open fun getUser(id: String): UserResponse? = userRepository.findById(id)

    @CircuitBreaker("default")
    @Retry("default")
    @Timeout("default")
    open fun getUsers(page: Int, size: Int, sort: String): List<UserResponse> =
        userRepository.findAll()
            .sortedWith(getComparator(sort))
            .drop(page * size)
            .take(size)

    @CircuitBreaker("default")
    open fun updateUser(id: String, request: UserRequest): UserResponse {
        if (!userRepository.update(id, request.name, request.email)) {
            throw HttpServerResponseException.of(404, "User not found")
        }
        return UserResponse(id, request.name, request.email, LocalDateTime.now())
    }

    @Timeout("default")
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
