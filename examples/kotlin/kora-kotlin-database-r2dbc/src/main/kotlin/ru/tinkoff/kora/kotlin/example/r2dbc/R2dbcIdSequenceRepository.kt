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
interface R2dbcIdSequenceRepository : R2dbcRepository {
    data class Entity(val id: Long?, @field:Column("name") val name: String) {
        constructor(name: String) : this(null, name)
    }

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


