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
import java.util.UUID

@Repository
interface JdbcCrudExtendedCompositeRepository :
    AbstractJdbcCrudRepository<JdbcCrudExtendedCompositeRepository.Entity.EntityId, JdbcCrudExtendedCompositeRepository.Entity> {

    @EntityJdbc
    @Table("entities_composite_uuid")
    data class Entity(
        @field:Id @field:Embedded val id: EntityId,
        @field:Column("name") val name: String
    ) {
        data class EntityId(val a: UUID = UUID.randomUUID(), val b: UUID = UUID.randomUUID())
    }

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE %{id#where}")
    fun findById(id: Entity.EntityId): Entity?

    @Query("DELETE FROM entities_composite_uuid WHERE %{id#where}")
    fun deleteById(id: Entity.EntityId): UpdateCount

    @Query("DELETE FROM entities_composite_uuid")
    fun deleteAll(): UpdateCount
}


