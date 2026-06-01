package ru.tinkoff.kora.kotlin.example.jdbc

import jakarta.annotation.Nullable
import org.postgresql.util.PGobject
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.Module
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.*
import ru.tinkoff.kora.database.jdbc.EntityJdbc
import ru.tinkoff.kora.database.jdbc.JdbcRepository
import ru.tinkoff.kora.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultColumnMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcResultSetMapper
import ru.tinkoff.kora.database.jdbc.mapper.result.JdbcRowMapper
import ru.tinkoff.kora.json.common.JsonReader
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.json.common.annotation.Json
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

@Repository
interface JdbcCrudSuspendRepository : JdbcRepository {

    @EntityJdbc
    @Table("entities")
    data class Entity(
        @field:Id val id: String,
        @field:Column("value1") val field1: Int,
        val value2: String,
        @field:Nullable val value3: String?
    )

    @Query("SELECT * FROM entities WHERE id = :id")
    suspend fun findById(id: String): Entity?

    @Query("SELECT * FROM entities")
    suspend fun findAll(): List<Entity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    suspend fun insert(entity: Entity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    suspend fun update(entity: Entity)

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM entities")
    suspend fun deleteAll(): UpdateCount
}


