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
interface JdbcCrudSyncRepository : JdbcRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): JdbcEntity?

    @Query("SELECT * FROM entities")
    fun findAll(): List<JdbcEntity>

    @Query("SELECT * FROM entities WHERE id = ANY(:ids)")
    fun findAllByIds(@Mapping(ListOfStringJdbcParameterMapper::class) ids: List<String>): List<JdbcEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: JdbcEntity)

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insertBatch(@Batch entity: List<JdbcEntity>): UpdateCount

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun update(entity: JdbcEntity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun updateBatch(@Batch entity: List<JdbcEntity>): UpdateCount

    @Query("DELETE FROM entities WHERE id = :id")
    fun deleteById(id: String)

    @Query("DELETE FROM entities")
    fun deleteAll(): UpdateCount
}

@EntityJdbc
data class JdbcEntity(
    val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    val value3: String?
)

@Component
class ListOfStringJdbcParameterMapper : JdbcParameterColumnMapper<List<String>> {
    // the 2.0 contract declares the value as @Nullable, which Kotlin enforces on the override
    override fun set(stmt: PreparedStatement, index: Int, value: List<String>?) {
        if (value == null) {
            stmt.setNull(index, Types.ARRAY)
            return
        }
        stmt.setArray(index, stmt.connection.createArrayOf("VARCHAR", value.toTypedArray()))
    }
}

