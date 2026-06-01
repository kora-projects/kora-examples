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
interface JdbcMapperColumnRepository : JdbcRepository {

    @EntityJdbc
    data class Entity(
        val id: String,
        @Mapping(JdbcEntityFieldTypeResultMapper::class)
        @Mapping(JdbcEntityFieldTypeColumnMapper::class)
        @field:Column("value1")
        val field1: FieldType,
        val value2: String,
        @field:Nullable val value3: String?
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

