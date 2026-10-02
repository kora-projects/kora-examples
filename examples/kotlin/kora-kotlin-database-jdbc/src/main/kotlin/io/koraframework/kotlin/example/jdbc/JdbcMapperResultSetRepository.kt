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
interface JdbcMapperResultSetRepository : JdbcRepository {

    @Mapping(JdbcEntityPartResultSetMapper::class)
    @Query("SELECT id, value1 FROM entities")
    fun findAllParts(): Map<Int, List<JdbcEntityPart>>
}

class JdbcEntityPartResultSetMapper : JdbcResultSetMapper<Map<Int, List<JdbcEntityPart>>> {
    override fun apply(rs: ResultSet): Map<Int, List<JdbcEntityPart>> {
        val result = LinkedHashMap<Int, MutableList<JdbcEntityPart>>()
        while (rs.next()) {
            val entityPart = JdbcEntityPart(rs.getString(1), rs.getInt(2))
            result.computeIfAbsent(entityPart.field1) { ArrayList() }.add(entityPart)
        }
        return result
    }
}

