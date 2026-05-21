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
interface JdbcIdSequenceRepository : JdbcRepository {
    @EntityJdbc
    data class Entity(@field:Id val id: Long?, @field:Column("name") val name: String) {
        constructor(name: String) : this(null, name)
    }

    @Query("SELECT * FROM entities_sequence WHERE id = :id")
    fun findById(id: Long): Entity?

    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name) RETURNING id")
    fun insert(entity: Entity): Long

    @Id
    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name)")
    fun insertGenerated(entity: Entity): Long

    @Id
    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name)")
    fun insertGenerated(@Batch entity: List<Entity>): List<Long>
}


