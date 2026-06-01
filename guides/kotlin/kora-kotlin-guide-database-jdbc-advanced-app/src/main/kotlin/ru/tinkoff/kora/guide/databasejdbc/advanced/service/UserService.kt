package ru.tinkoff.kora.guide.databasejdbc.advanced.service

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.databasejdbc.advanced.dto.UserRequest
import ru.tinkoff.kora.guide.databasejdbc.advanced.dto.UserResponse
import ru.tinkoff.kora.guide.databasejdbc.advanced.repository.UserDAO
import ru.tinkoff.kora.guide.databasejdbc.advanced.repository.UserRepository
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import java.time.LocalDateTime

@Component
class UserService(private val userRepository: UserRepository) {

    fun createUser(request: UserRequest): UserResponse {
        val generatedId = userRepository.save(request.name, request.email)
        return UserResponse(generatedId.toString(), request.name, request.email, LocalDateTime.now())
    }

    fun getUser(id: String): UserResponse? =
        id.toLongOrNull()?.let { userRepository.findById(it) }?.let { toResponse(it) }

    fun getUsers(page: Int, size: Int, sort: String): List<UserResponse> =
        userRepository.findAll()
            .map { toResponse(it) }
            .sortedWith(getComparator(sort))
            .drop(page * size)
            .take(size)

    fun updateUser(id: String, request: UserRequest): UserResponse {
        val parsedId = parseIdOrThrow(id)
        val updated = userRepository.update(parsedId, request.name, request.email)
        if (updated.value() < 1) {
            throw HttpServerResponseException.of(404, "User not found")
        }
        return UserResponse(parsedId.toString(), request.name, request.email, LocalDateTime.now())
    }

    fun deleteUser(id: String) {
        val parsedId = parseIdOrThrow(id)
        val deleted = userRepository.deleteById(parsedId)
        if (deleted.value() < 1) {
            throw HttpServerResponseException.of(404, "User not found")
        }
    }

    private fun parseIdOrThrow(id: String): Long =
        id.toLongOrNull() ?: throw HttpServerResponseException.of(400, "Invalid user id: $id")

    private fun toResponse(user: UserDAO): UserResponse =
        UserResponse(user.id.toString(), user.name, user.email, user.createdAt)

    private fun getComparator(sort: String): Comparator<UserResponse> = when (sort.lowercase()) {
        "name" -> compareBy { it.name }
        "email" -> compareBy { it.email }
        "createdat" -> compareBy { it.createdAt }
        else -> compareBy { it.name }
    }
}
