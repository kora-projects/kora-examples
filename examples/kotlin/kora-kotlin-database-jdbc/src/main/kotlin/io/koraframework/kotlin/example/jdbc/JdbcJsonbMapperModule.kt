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
                jsonb.value = writer.toString(value)
                stmt.setObject(index, jsonb)
            }
        }
    }

    @Json
    fun <T> jdbcJsonResultColumnMapper(reader: JsonReader<T>): JdbcResultColumnMapper<T> {
        return JdbcResultColumnMapper { row, index ->
            val value = row.getString(index)
            if (value == null) null else reader.read(value)
        }
    }
}


