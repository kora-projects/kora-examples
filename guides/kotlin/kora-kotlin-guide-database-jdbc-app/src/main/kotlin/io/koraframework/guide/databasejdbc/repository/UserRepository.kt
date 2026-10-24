package io.koraframework.guide.databasejdbc.repository

import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.jdbc.JdbcRepository

@Repository
interface UserRepository : JdbcRepository {

    @Query("SELECT id, name, email, created_at FROM users ORDER BY id")
    fun findAll(): List<UserDAO>

    @Query("SELECT id, name, email, created_at FROM users WHERE id = :id")
    fun findById(id: Long): UserDAO?

    @Query("INSERT INTO users(name, email) VALUES (:name, :email) RETURNING id")
    fun save(name: String, email: String): Long

    @Query("UPDATE users SET name = :name, email = :email WHERE id = :id")
    fun update(id: Long, name: String, email: String): UpdateCount

    @Query("DELETE FROM users WHERE id = :id")
    fun deleteById(id: Long): UpdateCount
}
