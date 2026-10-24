package io.koraframework.guide.databasejdbc.repository

import io.koraframework.database.common.annotation.Column
import io.koraframework.database.jdbc.annotation.EntityJdbc
import java.time.LocalDateTime

@EntityJdbc
data class UserDAO(
    @field:Column("id") val id: Long,
    @field:Column("name") val name: String,
    @field:Column("email") val email: String,
    @field:Column("created_at") val createdAt: LocalDateTime,
)
