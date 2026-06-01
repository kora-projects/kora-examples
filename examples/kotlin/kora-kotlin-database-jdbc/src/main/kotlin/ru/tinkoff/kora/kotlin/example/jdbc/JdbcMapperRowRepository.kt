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
interface JdbcMapperRowRepository : JdbcRepository {

    @Mapping(JdbcEntityPartRowMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): List<JdbcEntityPart>
}

@EntityJdbc
data class JdbcEntityPart(val id: String, @field:Column("value1") val field1: Int)

class JdbcEntityPartRowMapper : JdbcRowMapper<JdbcEntityPart> {
    override fun apply(rs: ResultSet): JdbcEntityPart {
        return JdbcEntityPart(rs.getString(1), rs.getInt(2))
    }
}

