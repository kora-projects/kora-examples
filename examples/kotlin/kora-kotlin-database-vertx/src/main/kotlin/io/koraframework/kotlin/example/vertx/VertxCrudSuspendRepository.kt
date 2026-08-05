package io.koraframework.kotlin.example.vertx

import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Root
import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.Batch
import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.vertx.VertxRepository
import io.koraframework.database.vertx.mapper.parameter.VertxParameterColumnMapper
import io.koraframework.database.vertx.mapper.result.VertxResultColumnMapper
import io.koraframework.database.vertx.mapper.result.VertxRowMapper
import io.koraframework.database.vertx.mapper.result.VertxRowSetMapper

@Repository
interface VertxCrudSuspendRepository : VertxRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    suspend fun findById(id: String): VertxEntity?

    @Query("SELECT * FROM entities")
    suspend fun findAll(): List<VertxEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    suspend fun insert(entity: VertxEntity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    suspend fun update(entity: VertxEntity)

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM entities")
    suspend fun deleteAll(): UpdateCount
}

