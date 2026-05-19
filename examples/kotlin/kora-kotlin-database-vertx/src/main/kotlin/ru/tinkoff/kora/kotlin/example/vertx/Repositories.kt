package ru.tinkoff.kora.kotlin.example.vertx

import jakarta.annotation.Nullable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.database.common.UpdateCount
import ru.tinkoff.kora.database.common.annotation.Batch
import ru.tinkoff.kora.database.common.annotation.Column
import ru.tinkoff.kora.database.common.annotation.Query
import ru.tinkoff.kora.database.common.annotation.Repository
import ru.tinkoff.kora.database.vertx.VertxRepository

data class VertxEntity(
    val id: String,
    @field:Column("value1") val field1: Int,
    val value2: String,
    @field:Nullable val value3: String?
)

@Repository
interface VertxCrudSyncRepository : VertxRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): VertxEntity?

    @Query("SELECT * FROM entities")
    fun findAll(): List<VertxEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: VertxEntity)

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insertBatch(@Batch entity: List<VertxEntity>): UpdateCount

    @Query("DELETE FROM entities")
    fun deleteAll(): UpdateCount
}

@Repository
interface VertxCrudReactorRepository : VertxRepository {
    @Query("SELECT * FROM entities WHERE id = :id")
    fun findById(id: String): Mono<VertxEntity>

    @Query("SELECT * FROM entities")
    fun findAll(): Flux<VertxEntity>

    @Query("INSERT INTO entities(id, value1, value2, value3) VALUES (:entity.id, :entity.field1, :entity.value2, :entity.value3)")
    fun insert(entity: VertxEntity): Mono<Void>

    @Query("DELETE FROM entities")
    fun deleteAll(): Mono<UpdateCount>
}

@Root
@Component
class RootService(
    private val vertxCrudRepository: VertxCrudSyncRepository
)
