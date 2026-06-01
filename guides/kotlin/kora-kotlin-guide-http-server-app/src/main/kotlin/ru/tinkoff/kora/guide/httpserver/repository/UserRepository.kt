package ru.tinkoff.kora.guide.httpserver.repository

import ru.tinkoff.kora.guide.httpserver.dto.UserResponse

interface UserRepository {
    fun findAll(): List<UserResponse>
    fun findById(id: String): UserResponse?
    fun save(name: String, email: String): String
    fun update(id: String, name: String, email: String): Boolean
    fun deleteById(id: String): Boolean
}
