package ru.tinkoff.kora.guide.databasecassandra.service

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.databasecassandra.dto.UserRequest
import ru.tinkoff.kora.guide.databasecassandra.dto.UserResponse
import ru.tinkoff.kora.guide.databasecassandra.repository.UserDAO
import ru.tinkoff.kora.guide.databasecassandra.repository.UserRepository
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import java.time.Instant
import java.util.*

@Component
class UserService(private val userRepository: UserRepository) {

    fun createUser(request: UserRequest): UserResponse {
        val user = UserDAO(UUID.randomUUID().toString(), request.name, request.email, Instant.now())
        userRepository.save(user)
        return toResponse(user)
    }

    fun getUser(id: String): UserResponse? = userRepository.findById(id)?.let(::toResponse)

    fun getUsers(page: Int, size: Int, sort: String): List<UserResponse> =
        userRepository.findAll()
            .map(::toResponse)
            .sortedWith(getComparator(sort))
            .drop(page * size)
            .take(size)

    fun updateUser(id: String, request: UserRequest): UserResponse {
        val existing = userRepository.findById(id) ?: throw HttpServerResponseException.of(404, "User not found")
        val updated = UserDAO(id, request.name, request.email, existing.createdAt)
        userRepository.update(updated)
        return toResponse(updated)
    }

    fun deleteUser(id: String) {
        if (userRepository.findById(id) == null) {
            throw HttpServerResponseException.of(404, "User not found")
        }
        userRepository.deleteById(id)
    }

    private fun toResponse(user: UserDAO): UserResponse =
        UserResponse(user.id, user.name, user.email, user.createdAt)

    private fun getComparator(sort: String): Comparator<UserResponse> = when (sort.lowercase()) {
        "name" -> compareBy { it.name }
        "email" -> compareBy { it.email }
        "createdat" -> compareBy { it.createdAt }
        else -> compareBy { it.name }
    }
}
