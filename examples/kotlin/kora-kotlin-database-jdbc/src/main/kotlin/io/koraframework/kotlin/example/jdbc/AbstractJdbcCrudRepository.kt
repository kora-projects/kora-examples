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


