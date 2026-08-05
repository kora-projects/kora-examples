package io.koraframework.guide.observability.repository

import io.koraframework.guide.observability.dto.UserResponse

interface UserRepository {
    fun save(name: String, email: String): String
    fun findById(id: String): UserResponse?
    fun findAll(): List<UserResponse>
    fun update(id: String, name: String, email: String): Boolean
    fun deleteById(id: String): Boolean
}

