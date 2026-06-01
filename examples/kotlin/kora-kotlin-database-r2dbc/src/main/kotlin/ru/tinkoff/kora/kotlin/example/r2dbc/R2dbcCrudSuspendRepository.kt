package ru.tinkoff.kora.kotlin.example.r2dbc

import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository

@Repository
interface R2dbcCrudSuspendRepository : R2dbcRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    suspend fun findById(id: String): R2dbcEntity?

    @Query("SELECT * FROM entities")
    suspend fun findAll(): List<R2dbcEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    suspend fun insert(entity: R2dbcEntity)

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    suspend fun update(entity: R2dbcEntity)

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM entities")
    suspend fun deleteAll(): UpdateCount
}


