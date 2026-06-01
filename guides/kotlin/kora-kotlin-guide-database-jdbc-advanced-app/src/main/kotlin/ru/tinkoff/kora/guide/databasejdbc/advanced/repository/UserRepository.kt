package ru.tinkoff.kora.guide.databasejdbc.advanced.repository

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.jdbc.JdbcRepository

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
