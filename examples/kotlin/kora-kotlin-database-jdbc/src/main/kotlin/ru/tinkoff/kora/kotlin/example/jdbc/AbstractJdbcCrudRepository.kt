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

interface AbstractJdbcCrudRepository<K, V> : JdbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table}")
    fun findAll(): List<V>

    @Query("INSERT INTO %{entity#inserts}")
    fun insert(entity: V): UpdateCount

    @Query("INSERT INTO %{entity#inserts}")
    fun insertBatch(@Batch entity: List<V>): UpdateCount

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun update(entity: V): UpdateCount

    @Query("UPDATE %{entity#table} SET %{entity#updates} WHERE %{entity#where = @id}")
    fun updateBatch(@Batch entity: List<V>): UpdateCount

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (%{entity#selects=@id}) DO UPDATE SET %{entity#updates}")
    fun upsert(entity: V): UpdateCount

    @Query("INSERT INTO %{entity#inserts} ON CONFLICT (%{entity#selects=@id}) DO UPDATE SET %{entity#updates}")
    fun upsertBatch(@Batch entity: List<V>): UpdateCount

    @Query("DELETE FROM %{entity#table} WHERE %{entity#where = @id}")
    fun delete(entity: V): UpdateCount

    @Query("DELETE FROM %{entity#table} WHERE %{entity#where = @id}")
    fun deleteBatch(@Batch entity: List<V>): UpdateCount
}


