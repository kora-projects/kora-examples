package io.koraframework.kotlin.example.r2dbc

import io.koraframework.database.common.annotation.*
import io.koraframework.database.r2dbc.R2dbcRepository

@Repository
interface R2dbcIdSequenceRepository : R2dbcRepository {

    data class Entity(val id: Long?, @field:Column("name") val name: String)

    @Query("SELECT * FROM entities_sequence WHERE id = :id")
    suspend fun findById(id: Long): Entity?

    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name) RETURNING id")
    suspend fun insert(entity: Entity): Long

    @Id
    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name)")
    suspend fun createGenerated(entity: Entity): Long

    @Id
    @Query("INSERT INTO entities_sequence(name) VALUES (:entity.name)")
    suspend fun createGenerated(@Batch entity: List<Entity>): List<Long>
}


