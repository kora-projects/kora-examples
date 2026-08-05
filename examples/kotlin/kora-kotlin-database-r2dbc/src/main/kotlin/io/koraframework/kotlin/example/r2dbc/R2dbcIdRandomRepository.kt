package io.koraframework.kotlin.example.r2dbc

import io.koraframework.database.common.UpdateCount
import io.koraframework.database.common.annotation.Column
import io.koraframework.database.common.annotation.Query
import io.koraframework.database.common.annotation.Repository
import io.koraframework.database.r2dbc.R2dbcRepository
import java.util.*

@Repository
interface R2dbcIdRandomRepository : R2dbcRepository {

    data class Entity(val id: UUID = UUID.randomUUID(), @field:Column("name") val name: String)

    @Query("SELECT * FROM entities_uuid WHERE id = :id")
    suspend fun findById(id: UUID): Entity?

    @Query("INSERT INTO entities_uuid(id, name) VALUES (:entity.id, :entity.name)")
    suspend fun insert(entity: Entity): UpdateCount
}


