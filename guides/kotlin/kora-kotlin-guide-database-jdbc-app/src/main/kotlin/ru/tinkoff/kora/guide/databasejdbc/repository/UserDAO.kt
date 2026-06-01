package ru.tinkoff.kora.guide.databasejdbc.repository

import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.jdbc.EntityJdbc
import java.time.LocalDateTime

@EntityJdbc
data class UserDAO(
    @field:Column("id") val id: Long,
    @field:Column("name") val name: String,
    @field:Column("email") val email: String,
    @field:Column("created_at") val createdAt: LocalDateTime,
)
