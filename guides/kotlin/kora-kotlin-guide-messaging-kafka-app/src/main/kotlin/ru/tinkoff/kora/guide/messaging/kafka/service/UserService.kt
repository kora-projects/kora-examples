package ru.tinkoff.kora.guide.messaging.kafka.service

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserRequest
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserResponse
import ru.tinkoff.kora.guide.messaging.kafka.kafka.UserCreatedEvent
import ru.tinkoff.kora.guide.messaging.kafka.repository.UserRepository
import ru.tinkoff.kora.http.server.common.HttpServerResponseException

@Component
class UserService(
    private val userRepository: UserRepository
) {

    fun createUser(event: UserCreatedEvent) {
        userRepository.save(UserResponse(event.id, event.name, event.email, event.createdAt))
    }

    fun getUser(id: String): UserResponse? = userRepository.findById(id)

    fun getUsers(page: Int, size: Int, sort: String): List<UserResponse> {
        return userRepository.findAll()
            .sortedWith(getComparator(sort))
            .drop(page * size)
            .take(size)
    }

    fun updateUser(id: String, request: UserRequest): UserResponse {
        val updated = userRepository.update(id, request.name, request.email)
        if (!updated) {
            throw HttpServerResponseException.of(404, "User not found")
        }
        return userRepository.findById(id) ?: throw HttpServerResponseException.of(404, "User not found")
    }

    fun deleteUser(id: String) {
        val deleted = userRepository.deleteById(id)
        if (!deleted) {
            throw HttpServerResponseException.of(404, "User not found")
        }
    }

    private fun getComparator(sort: String): Comparator<UserResponse> {
        return when (sort.lowercase()) {
            "name" -> compareBy(UserResponse::name)
            "email" -> compareBy(UserResponse::email)
            "createdat" -> compareBy(UserResponse::createdAt)
            else -> compareBy(UserResponse::name)
        }
    }
}
