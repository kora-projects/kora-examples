package io.koraframework.kotlin.example.r2dbc

import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.*
import io.koraframework.database.r2dbc.R2dbcRepository

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
    val value3: String?
)

