package ru.tinkoff.kora.guide.databasecassandra.repository

import ru.tinkoff.kora.database.cassandra.CassandraRepository
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository

@Repository
interface UserRepository : CassandraRepository {

    @Query("SELECT id, name, email, created_at FROM users")
    fun findAll(): List<UserDAO>

    @Query("SELECT id, name, email, created_at FROM users WHERE id = :id")
    fun findById(id: String): UserDAO?

    @Query(
        """
        INSERT INTO users(id, name, email, created_at)
        VALUES (:user.id, :user.name, :user.email, :user.createdAt)
        """
    )
    fun save(user: UserDAO)

    @Query(
        """
        UPDATE users
        SET name = :user.name, email = :user.email, created_at = :user.createdAt
        WHERE id = :user.id
        """
    )
    fun update(user: UserDAO)

    @Query("DELETE FROM users WHERE id = :id")
    fun deleteById(id: String)
}
