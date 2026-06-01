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
import java.util.UUID

@Repository
interface JdbcIdRandomCompositeRepository : JdbcRepository {

    @EntityJdbc
    data class Entity(
        @field:Id @field:Embedded val id: EntityId = EntityId(),
        @field:Column("name") val name: String
    ) {
        constructor(name: String) : this(EntityId(), name)

        data class EntityId(val a: UUID = UUID.randomUUID(), val b: UUID = UUID.randomUUID())
    }

    @Query("SELECT * FROM entities_composite_uuid WHERE a = :id.a AND b = :id.b")
    fun findById(id: Entity.EntityId): Entity?

    @Query("INSERT INTO entities_composite_uuid(a, b, name) VALUES (:entity.id.a, :entity.id.b, :entity.name)")
    fun insert(entity: Entity): UpdateCount

    @Query("INSERT INTO entities_composite_uuid(a, b, name) VALUES (:entity.id.a, :entity.id.b, :entity.name)")
    fun insert(@Batch entity: List<Entity>): UpdateCount
}


