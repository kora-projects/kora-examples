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
import java.util.UUID

@Repository
interface R2dbcIdRandomRepository : R2dbcRepository {

    data class Entity(val id: UUID = UUID.randomUUID(), @field:Column("name") val name: String)

    @Query("SELECT * FROM entities_uuid WHERE id = :id")
    suspend fun findById(id: UUID): Entity?

    @Query("INSERT INTO entities_uuid(id, name) VALUES (:entity.id, :entity.name)")
    suspend fun insert(entity: Entity): UpdateCount
}


