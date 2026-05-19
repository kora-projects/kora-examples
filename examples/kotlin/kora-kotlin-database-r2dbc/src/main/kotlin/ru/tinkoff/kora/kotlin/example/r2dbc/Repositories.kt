package ru.tinkoff.kora.kotlin.example.r2dbc

import io.r2dbc.spi.Row
import io.r2dbc.spi.Statement
import jakarta.annotation.Nullable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Id
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.common.annotation.Table
import ru.tinkoff.kora.database.r2dbc.R2dbcRepository
import ru.tinkoff.kora.database.r2dbc.mapper.parameter.R2dbcParameterColumnMapper
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcResultColumnMapper
import ru.tinkoff.kora.database.r2dbc.mapper.result.R2dbcRowMapper
import java.util.UUID

data class R2dbcEntity(
    val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

@Repository
interface R2dbcCrudRepository : R2dbcRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): Mono<R2dbcEntity>

    @Query("SELECT * FROM entities")
    fun findAll(): Flux<R2dbcEntity>

    @Query("SELECT * FROM entities")
    fun findAllMono(): Mono<List<R2dbcEntity>>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: R2dbcEntity): Mono<Void>

    @Query("UPDATE entities SET value1 = :entity.field1, value2 = :entity.value2, value3 = :entity.value3 WHERE id = :entity.id")
    fun update(entity: R2dbcEntity): Mono<Void>

    @Query("DELETE FROM entities WHERE id = :id")
    fun deleteById(id: String): Mono<Void>

    @Query("DELETE FROM entities")
    fun deleteAll(): Mono<UpdateCount>
}

@Repository
interface R2dbcCrudSyncRepository : R2dbcRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): R2dbcEntity?

    @Query("SELECT * FROM entities")
    fun findAll(): List<R2dbcEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: R2dbcEntity)

    @Query("DELETE FROM entities")
    fun deleteAll(): UpdateCount
}

@Table("entities")
data class R2dbcMacrosEntity(
    @field:Id val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

@Repository
interface R2dbcCrudMacrosRepository : R2dbcRepository {
    @Query("SELECT %{return#selects} FROM %{return#table} WHERE id = :id")
    fun findById(id: String): Mono<R2dbcMacrosEntity>

    @Query("SELECT %{return#selects} FROM %{return#table}")
    fun findAll(): Flux<R2dbcMacrosEntity>

    @Query("INSERT INTO %{entity#inserts}")
    fun insert(entity: R2dbcMacrosEntity): Mono<Void>

    @Query("DELETE FROM entities")
    fun deleteAll(): Mono<UpdateCount>
}

@Repository
interface R2dbcIdRandomRepository : R2dbcRepository {
    data class Entity(val id: UUID = UUID.randomUUID(), @field:Column("name") val name: String)

    @Query("SELECT * FROM entities_uuid WHERE id = :id")
    fun findById(id: UUID): Mono<Entity>

    @Query("INSERT INTO entities_uuid(id, name) VALUES (:entity.id, :entity.name)")
    fun insert(entity: Entity): Mono<UpdateCount>
}

@Root
@Component
class RootService(
    private val r2dbcCrudSyncRepository: R2dbcCrudSyncRepository
)
