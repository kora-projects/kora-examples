package ru.tinkoff.kora.guide.grpcserver.advanced.service

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserRequest
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserResponse
import ru.tinkoff.kora.guide.grpcserver.advanced.repository.UserRepository
import java.time.LocalDateTime

@Component
class UserService(
    private val userRepository: UserRepository
) {

    fun createUser(request: UserRequest): UserResponse {
        val generatedId = userRepository.save(request.name, request.email)
        return UserResponse(generatedId, request.name, request.email, LocalDateTime.now())
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
            throw UserNotFoundException(id)
        }
        return UserResponse(id, request.name, request.email, LocalDateTime.now())
    }

    fun deleteUser(id: String) {
        val deleted = userRepository.deleteById(id)
        if (!deleted) {
            throw UserNotFoundException(id)
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
