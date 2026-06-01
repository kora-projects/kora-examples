package ru.tinkoff.kora.guide.cache.repository

import ru.tinkoff.kora.guide.cache.dto.UserResponse

interface UserRepository {
    fun save(name: String, email: String): String
    fun findById(id: String): UserResponse?
    fun findAll(): List<UserResponse>
    fun update(id: String, name: String, email: String): Boolean
    fun deleteById(id: String): Boolean
}
