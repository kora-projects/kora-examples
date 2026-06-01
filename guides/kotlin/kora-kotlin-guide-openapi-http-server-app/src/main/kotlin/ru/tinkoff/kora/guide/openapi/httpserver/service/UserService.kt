package ru.tinkoff.kora.guide.openapi.httpserver.service

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.openapi.httpserver.dto.UserRequest
import ru.tinkoff.kora.guide.openapi.httpserver.dto.UserResponse
import ru.tinkoff.kora.guide.openapi.httpserver.repository.UserRepository
import java.time.LocalDateTime

@Component
class UserService(private val userRepository: UserRepository) {
    fun createUser(request: UserRequest): UserResponse {
        val id = userRepository.save(request.name, request.email)
        return UserResponse(id, request.name, request.email, LocalDateTime.now())
    }

    fun getUser(id: String): UserResponse? = userRepository.findById(id)
    fun getUsers(page: Int, size: Int, sort: String): List<UserResponse> = userRepository.findAll()
        .sortedWith(getComparator(sort))
        .drop(page * size)
        .take(size)

    fun updateUser(id: String, request: UserRequest): UserResponse {
        userRepository.update(id, request.name, request.email)
        return UserResponse(id, request.name, request.email, LocalDateTime.now())
    }

    fun deleteUser(id: String) {
        userRepository.deleteById(id)
    }

    private fun getComparator(sort: String): Comparator<UserResponse> = when (sort.lowercase()) {
        "name" -> compareBy { it.name }
        "email" -> compareBy { it.email }
        "createdat" -> compareBy { it.createdAt }
        else -> compareBy { it.name }
    }
}
