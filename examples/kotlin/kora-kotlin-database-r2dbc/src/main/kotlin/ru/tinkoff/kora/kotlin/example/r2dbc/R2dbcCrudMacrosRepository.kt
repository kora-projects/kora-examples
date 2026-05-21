package ru.tinkoff.kora.kotlin.example.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import jakarta.annotation.Nullable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.*
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.tinkoff.kora.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcResultColumnMapper
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcRowMapper

@Repository
interface R2dbcCrudMacrosRepository : R2dbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    suspend fun findById(id: String): R2dbcMacrosEntity?

    @Query("SELECT %{return#selects} FROM %{return#table}")
    suspend fun findAll(): List<R2dbcMacrosEntity>

    @Query("INSERT INTO %{entity#inserts}")
    suspend fun insert(entity: R2dbcMacrosEntity)

    @Query("UPDATE %{entity#updates} WHERE %{entity#where = @id}")
    suspend fun update(entity: R2dbcMacrosEntity)

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM entities")
    suspend fun deleteAll(): UpdateCount
}

@Table("entities")
data class R2dbcMacrosEntity(
    @field:Id val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

