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
interface JdbcCrudExtendedRepository : AbstractJdbcCrudRepository<String, JdbcCrudExtendedRepository.Entity> {

    @EntityJdbc
    @Table("entities")
    data class Entity(
        @field:Id val id: String,
        @field:Column("value1") val field1: Int,
        val value2: String,
        val value3: String?
    )

    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    fun findById(id: String): Entity?

    @Query("DELETE FROM entities WHERE id = :id")
    fun deleteById(id: String): UpdateCount

    @Query("DELETE FROM entities")
    fun deleteAll(): UpdateCount
}


