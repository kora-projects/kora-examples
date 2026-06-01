package ru.tinkoff.kora.guide.grpcserver.advanced.repository

import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserResponse

interface UserRepository {
    fun findAll(): List<UserResponse>

    fun findById(id: String): UserResponse?

    fun save(name: String, email: String): String

    fun update(id: String, name: String, email: String): Boolean

    fun deleteById(id: String): Boolean
}
