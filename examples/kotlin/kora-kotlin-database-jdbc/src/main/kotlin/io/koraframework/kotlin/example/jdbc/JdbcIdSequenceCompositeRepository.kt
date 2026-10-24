package io.koraframework.kotlin.example.jdbc

import org.postgresql.util.PGobject
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Module
import io.koraframework.common.annotation.Root
import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.*
import io.koraframework.database.jdbc.annotation.EntityJdbc
import io.koraframework.database.jdbc.JdbcRepository
import io.koraframework.database.jdbc.mapper.parameter.JdbcParameterColumnMapper
import io.koraframework.database.jdbc.mapper.result.JdbcResultColumnMapper
import io.koraframework.database.jdbc.mapper.result.JdbcResultSetMapper
import io.koraframework.database.jdbc.mapper.result.JdbcRowMapper
import io.koraframework.json.common.JsonReader
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.annotation.Json
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

@Repository
interface JdbcIdSequenceCompositeRepository : JdbcRepository {

    @EntityJdbc
    data class Entity(
        @field:Id @field:Embedded val id: EntityId?,
        @field:Column("name") val name: String
    ) {
        constructor(name: String) : this(null, name)

        @EntityJdbc
        data class EntityId(val a: Long?, val b: Long?)
    }

    @Query("SELECT * FROM entities_composite WHERE a = :id.a AND b = :id.b")
    fun findById(id: Entity.EntityId): Entity?

    @Query("INSERT INTO entities_composite(name) VALUES (:entity.name)")
    fun insert(entity: Entity): UpdateCount

    @Id
    @Query("INSERT INTO entities_composite(name) VALUES (:entity.name)")
    fun insertGenerated(entity: Entity): Entity.EntityId

    @Id
    @Query("INSERT INTO entities_composite(name) VALUES (:entity.name)")
    fun insertGenerated(@Batch entity: List<Entity>): List<Entity.EntityId>
}


