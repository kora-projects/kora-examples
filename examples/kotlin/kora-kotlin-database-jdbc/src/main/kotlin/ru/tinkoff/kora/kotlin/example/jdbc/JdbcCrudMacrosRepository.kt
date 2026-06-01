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
interface JdbcCrudMacrosRepository : JdbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    fun findById(id: String): JdbcMacrosEntity?

    @Query("SELECT %{return#selects} FROM %{return#table}")
    fun findAll(): List<JdbcMacrosEntity>

    @Query("INSERT INTO %{entity#inserts}")
    fun insert(entity: JdbcMacrosEntity): UpdateCount

    @Query("INSERT INTO %{entity#inserts}")
    fun insertBatch(@Batch entity: List<JdbcMacrosEntity>): UpdateCount

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun update(entity: JdbcMacrosEntity): UpdateCount

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun updateBatch(@Batch entity: List<JdbcMacrosEntity>): UpdateCount

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (id) DO UPDATE SET %{entity#updates}")
    fun upsert(entity: JdbcMacrosEntity): UpdateCount

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (id) DO UPDATE SET %{entity#updates}")
    fun upsertBatch(@Batch entity: List<JdbcMacrosEntity>): UpdateCount

    @Query("DELETE FROM entities WHERE id = :id")
    fun deleteById(id: String): UpdateCount

    @Query("DELETE FROM entities")
    fun deleteAll(): UpdateCount
}

@Table("entities")
data class JdbcMacrosEntity(
    @field:Id val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

