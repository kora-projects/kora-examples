package io.koraframework.guide.databasecassandra.repository

import io.koraframework.database.cassandra.annotation.EntityCassandra
import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Table
import java.time.Instant

@EntityCassandra
@Table("users")
data class UserDAO(
    @field:Column("id") val id: String,
    @field:Column("name") val name: String,
    @field:Column("email") val email: String,
    @field:Column("created_at") val createdAt: Instant,
)
