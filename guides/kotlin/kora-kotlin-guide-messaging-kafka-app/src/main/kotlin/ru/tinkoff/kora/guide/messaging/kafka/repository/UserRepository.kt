package ru.tinkoff.kora.guide.messaging.kafka.repository

import ru.tinkoff.kora.guide.messaging.kafka.dto.UserResponse

interface UserRepository {
    fun save(user: UserResponse)

    fun findAll(): List<UserResponse>

    fun findById(id: String): UserResponse?

    fun update(id: String, name: String, email: String): Boolean

    fun deleteById(id: String): Boolean
}
