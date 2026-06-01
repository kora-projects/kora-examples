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

@Module
interface JdbcJsonbMapperModule {

    @Json
    fun <T> jdbcJsonParameterColumnMapper(writer: JsonWriter<T>): JdbcParameterColumnMapper<T> {
        return JdbcParameterColumnMapper { stmt, index, value ->
            if (value == null) {
                stmt.setNull(index, Types.NULL)
            } else {
                val jsonb = PGobject()
                jsonb.type = "jsonb"
                jsonb.value = writer.toStringUnchecked(value)
                stmt.setObject(index, jsonb)
            }
        }
    }

    @Json
    fun <T> jdbcJsonResultColumnMapper(reader: JsonReader<T>): JdbcResultColumnMapper<T> {
        return JdbcResultColumnMapper { row, index ->
            val value = row.getString(index)
            if (value == null) null else reader.readUnchecked(value)
        }
    }
}


