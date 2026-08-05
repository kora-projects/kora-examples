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
interface JdbcMapperColumnRepository : JdbcRepository {

    @EntityJdbc
    data class Entity(
        val id: String,
        @Mapping(JdbcEntityFieldTypeResultMapper::class)
        @Mapping(JdbcEntityFieldTypeColumnMapper::class)
        @field:Column("value1")
        val field1: FieldType,
        val value2: String,
        val value3: String?
    ) {
        enum class FieldType(val code: Int) {
            UNKNOWN(-10),
            ONE(1),
            TWO(2)
        }
    }

    @Query("SELECT * FROM entities")
    fun findAll(): List<Entity>
}

class JdbcEntityFieldTypeResultMapper : JdbcResultColumnMapper<JdbcMapperColumnRepository.Entity.FieldType> {
    override fun apply(rs: ResultSet, index: Int): JdbcMapperColumnRepository.Entity.FieldType {
        val fieldAsInt = rs.getInt(index)
        return JdbcMapperColumnRepository.Entity.FieldType.entries.firstOrNull { it.code == fieldAsInt }
            ?: JdbcMapperColumnRepository.Entity.FieldType.UNKNOWN
    }
}

class JdbcEntityFieldTypeColumnMapper : JdbcParameterColumnMapper<JdbcMapperColumnRepository.Entity.FieldType> {
    override fun set(stmt: PreparedStatement, index: Int, value: JdbcMapperColumnRepository.Entity.FieldType?) {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER)
        } else {
            stmt.setInt(index, value.code)
        }
    }
}

