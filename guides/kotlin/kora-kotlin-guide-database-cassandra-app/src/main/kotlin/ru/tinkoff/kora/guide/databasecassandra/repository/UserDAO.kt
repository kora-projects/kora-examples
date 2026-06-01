package ru.tinkoff.kora.guide.databasecassandra.repository

import ru.tinkoff.kora.database.cassandra.annotation.EntityCassandra
import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Table
import java.time.Instant

@EntityCassandra
@Table("users")
data class UserDAO(
    @field:Column("id") val id: String,
    @field:Column("name") val name: String,
    @field:Column("email") val email: String,
    @field:Column("created_at") val createdAt: Instant,
)
